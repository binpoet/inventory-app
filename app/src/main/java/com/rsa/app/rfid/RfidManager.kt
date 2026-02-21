package com.rsa.app.rfid

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.zebra.rfid.api3.ENUM_TRANSPORT
import com.zebra.rfid.api3.InvalidUsageException
import com.zebra.rfid.api3.OperationFailureException
import com.zebra.rfid.api3.RfidEventsListener
import com.zebra.rfid.api3.RfidReadEvents
import com.zebra.rfid.api3.RfidStatusEvents
import com.zebra.rfid.api3.ReaderDevice
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.START_TRIGGER_TYPE
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE
import com.zebra.rfid.api3.TagData
import com.zebra.rfid.api3.TriggerInfo
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import java.util.concurrent.Executors

/**
 * Manages Zebra RFID reader connection, configuration, and inventory.
 * Separates RFID logic from UI; callbacks are invoked on the main thread.
 *
 * Workflow:
 * 1. init(context) then connect() on background thread
 * 2. After onConnected(), call startInventory() / stopInventory()
 * 3. EPC values are reported via onEpcRead(epcHexString)
 * 4. Call disconnect() on pause/destroy
 */
class RfidManager(
    private val context: Context,
    private val callback: Callback
) {
    interface Callback {
        fun onConnected()
        fun onDisconnected()
        fun onConnectionError(message: String)
        fun onEpcRead(epcHexString: String)
        fun onInventoryStarted()
        fun onInventoryStopped()
        fun onError(message: String)
    }

    companion object {
        private const val TAG = "RfidManager"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var readers: Readers? = null

    @Volatile
    private var reader: RFIDReader? = null

    @Volatile
    private var readerDevice: ReaderDevice? = null

    @Volatile
    private var eventHandler: RfidEventsListener? = null

    @Volatile
    private var isInventoryRunning = false

    /**
     * Initialize the SDK Readers instance. Call once (e.g. in Activity onCreate).
     * connect() will try SERVICE_SERIAL, BLUETOOTH, then SERVICE_USB to find a reader.
     */
    fun init() {
        if (readers != null) return
        try {
            readers = Readers(context, ENUM_TRANSPORT.SERVICE_SERIAL)
        } catch (e: Exception) {
            Log.e(TAG, "Init failed", e)
            notifyOnMain { callback.onConnectionError("SDK init failed: ${e.message}") }
        }
    }

    /**
     * Discover readers and connect to the first available one.
     * Must be called from a background thread; callbacks are on main thread.
     */
    fun connect() {
        executor.execute {
            try {
                val r = readers ?: run {
                    notifyOnMain { callback.onConnectionError("Reader not initialized") }
                    return@execute
                }
                var list = r.GetAvailableRFIDReaderList() ?: emptyList()
                if (list.isEmpty()) {
                    try {
                        r.setTransport(ENUM_TRANSPORT.BLUETOOTH)
                        list = r.GetAvailableRFIDReaderList() ?: emptyList()
                    } catch (e: Exception) { Log.d(TAG, "BLUETOOTH transport: ${e.message}") }
                }
                if (list.isEmpty()) {
                    try {
                        r.setTransport(ENUM_TRANSPORT.SERVICE_USB)
                        list = r.GetAvailableRFIDReaderList() ?: emptyList()
                    } catch (e: Exception) { Log.d(TAG, "SERVICE_USB transport: ${e.message}") }
                }
                if (list.isEmpty()) {
                    try {
                        r.setTransport(ENUM_TRANSPORT.SERVICE_SERIAL)
                        list = r.GetAvailableRFIDReaderList() ?: emptyList()
                    } catch (e: Exception) { Log.d(TAG, "SERVICE_SERIAL transport: ${e.message}") }
                }
                if (list.isEmpty()) {
                    notifyOnMain { callback.onConnectionError("No RFID reader found") }
                    return@execute
                }
                val device = list[0]
                val rd = device.rfidReader
                if (rd.isConnected) {
                    notifyOnMain { callback.onConnected() }
                    return@execute
                }
                rd.connect()
                readerDevice = device
                reader = rd
                configureReader()
                notifyOnMain { callback.onConnected() }
            } catch (e: InvalidUsageException) {
                Log.e(TAG, "InvalidUsageException", e)
                notifyOnMain { callback.onConnectionError("Invalid usage: ${e.message}") }
            } catch (e: OperationFailureException) {
                Log.e(TAG, "OperationFailureException: ${e.vendorMessage}", e)
                notifyOnMain { callback.onConnectionError("Connection failed: ${e.vendorMessage}") }
            } catch (e: Exception) {
                Log.e(TAG, "Connect failed", e)
                notifyOnMain { callback.onConnectionError("Connect failed: ${e.message}") }
            }
        }
    }

    /**
     * Configure trigger and event listener. Called after connect() on background thread.
     */
    private fun configureReader() {
        val rd = reader ?: return
        if (!rd.isConnected) return
        try {
            val triggerInfo = TriggerInfo().apply {
                StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE)
                StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE)
            }
            val handler = RfidEventHandler(this)
            eventHandler = handler
            rd.Events.addEventsListener(handler)
            rd.Events.setHandheldEvent(true)
            rd.Events.setTagReadEvent(true)
            rd.Events.setAttachTagDataWithReadEvent(false)
            rd.Events.setReaderDisconnectEvent(true)
            rd.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
            rd.Config.setStartTrigger(triggerInfo.StartTrigger)
            rd.Config.setStopTrigger(triggerInfo.StopTrigger)
        } catch (e: InvalidUsageException) {
            Log.e(TAG, "Configure InvalidUsageException", e)
        } catch (e: OperationFailureException) {
            Log.e(TAG, "Configure OperationFailureException: ${e.vendorMessage}", e)
        }
    }

    /**
     * Start RFID inventory (continuous read). Safe to call from any thread.
     */
    fun startInventory() {
        executor.execute {
            val rd = reader
            if (rd == null || !rd.isConnected) {
                notifyOnMain { callback.onError("Reader not connected") }
                return@execute
            }
            try {
                rd.Actions.Inventory.perform()
                isInventoryRunning = true
                notifyOnMain { callback.onInventoryStarted() }
            } catch (e: InvalidUsageException) {
                Log.e(TAG, "StartInventory InvalidUsageException", e)
                notifyOnMain { callback.onError("Start scan failed: ${e.message}") }
            } catch (e: OperationFailureException) {
                Log.e(TAG, "StartInventory OperationFailureException: ${e.vendorMessage}", e)
                notifyOnMain { callback.onError("Start scan failed: ${e.vendorMessage}") }
            } catch (e: Exception) {
                Log.e(TAG, "StartInventory failed", e)
                notifyOnMain { callback.onError("Start scan failed: ${e.message}") }
            }
        }
    }

    /**
     * Stop RFID inventory. Safe to call from any thread.
     */
    fun stopInventory() {
        executor.execute {
            val rd = reader ?: return@execute
            try {
                rd.Actions.Inventory.stop()
                isInventoryRunning = false
                notifyOnMain { callback.onInventoryStopped() }
            } catch (e: Exception) {
                Log.e(TAG, "StopInventory failed", e)
                isInventoryRunning = false
                notifyOnMain { callback.onInventoryStopped() }
            }
        }
    }

    /**
     * Disconnect reader and dispose SDK. Call on Activity onPause/onDestroy.
     */
    fun disconnect() {
        executor.execute {
            try {
                val rd = reader
                if (rd != null && isInventoryRunning) {
                    try { rd.Actions.Inventory.stop() } catch (e: Exception) { Log.w(TAG, "stop inventory", e) }
                    isInventoryRunning = false
                }
                val ev = eventHandler
                if (rd != null && ev != null) {
                    try {
                        rd.Events.removeEventsListener(ev)
                    } catch (e: Exception) {
                        Log.w(TAG, "removeEventsListener", e)
                    }
                }
                if (rd != null) {
                    try {
                        if (rd.isConnected) rd.disconnect()
                    } catch (e: Exception) {
                        Log.w(TAG, "disconnect", e)
                    }
                    reader = null
                }
                readerDevice = null
                eventHandler = null
                readers?.Dispose()
                readers = null
                notifyOnMain { callback.onDisconnected() }
            } catch (e: Exception) {
                Log.e(TAG, "Disconnect failed", e)
                reader = null
                readers = null
                notifyOnMain { callback.onDisconnected() }
            }
        }
    }

    fun isConnected(): Boolean = reader?.isConnected == true
    fun isScanning(): Boolean = isInventoryRunning

    internal fun getReader(): RFIDReader? = reader
    internal fun getExecutor(): java.util.concurrent.Executor = executor
    internal fun notifyOnMain(block: () -> Unit) {
        mainHandler.post(block)
    }
    internal fun getCallback(): Callback = callback
}

/** Timeout (ms) when fetching read tags from reader (used by event handler). */
private const val GET_READ_TAGS_TIMEOUT_MS = 100

/**
 * Zebra SDK event handler. Runs on SDK thread; forwards EPC and status to RfidManager on main thread.
 */
private class RfidEventHandler(private val manager: RfidManager) : RfidEventsListener {

    override fun eventReadNotify(e: RfidReadEvents?) {
        val rd = manager.getReader() ?: return
        try {
            val tags: Array<TagData>? = rd.Actions.getReadTags(GET_READ_TAGS_TIMEOUT_MS)
            if (!tags.isNullOrEmpty()) {
                // Use first (or nearest) tag EPC; avoid duplicate updates by taking latest batch
                val tag = tags[0]
                val epc = tag.tagID ?: return
                manager.notifyOnMain { manager.getCallback().onEpcRead(epc) }
            }
        } catch (ex: Exception) {
            manager.notifyOnMain { manager.getCallback().onError("Read failed: ${ex.message}") }
        }
    }

    override fun eventStatusNotify(rfidStatusEvents: RfidStatusEvents?) {
        if (rfidStatusEvents == null) return
        val statusType = rfidStatusEvents.StatusEventData.statusEventType
        // Use toString() — STATUS_EVENT_TYPE.name may be private in the Zebra SDK
        val typeName = statusType?.toString() ?: "UNKNOWN"
        android.util.Log.d("RfidManager", "Status: $typeName")
        // Handle disconnect: SDK may notify; app should call disconnect() in onPause/onDestroy anyway.
        if (statusType != null && typeName.contains("DISCONNECTION", ignoreCase = true)) {
            manager.notifyOnMain { manager.getCallback().onDisconnected() }
        }
    }
}
