package de.berlindroid.zekompanion

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import de.berlindroid.zekompanion.BadgeManager.Companion.DEVICE_PRODUCT_NAME
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidBadgeManager(
    private val context: Context,
) : BadgeManager {

    companion object {
        const val ACTION_USB_PERMISSION = "ACTION_USB_PERMISSION"
        const val ACTION_USB_PERMISSION_REQUEST_CODE = 4711
        const val BAUD_RATE = 115200
        const val READ_BYTE_ARRAY_SIZE = 1024
        const val READ_WRITE_TIMEOUT = 3_000
    }

    private val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    override suspend fun sendPayload(payload: BadgePayload): Result<Int> {
        val device = manager.findConnectedBadge()

        return if (device == null) {
            informNoBadgeFound(manager)
        } else {
            if (!manager.hasPermission(device)) {
                askPermission(device)
            }

            val actualCommand = payload.toBadgeCommand()
            sendCommandToBadge(
                actualCommand,
            )
        }
    }

    override suspend fun readResponse(): Result<String> {
        val device = manager.findConnectedBadge()

        return if (device == null) {
            informNoBadgeFound(manager)
        } else {
            if (!manager.hasPermission(device)) {
                askPermission(device)
            }

            receiveResponseFromBadge()
        }
    }

    override fun isConnected(): Boolean = manager.findConnectedBadge() != null

    private fun <RETURN_TYPE> performReturnableActionOnBadge(
        action: (UsbSerialPort) -> RETURN_TYPE,
    ): Result<RETURN_TYPE> {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)

        val driver = availableDrivers.first()
        val port = driver.ports.last()
        val connection = manager.openDevice(driver.device)


        return kotlin.runCatching {
            port.open(connection)
            port.dtr = true
            port.setParameters(
                /* baudRate = */BAUD_RATE,
                /* dataBits = */UsbSerialPort.DATABITS_8,
                /* stopBits = */UsbSerialPort.STOPBITS_1,
                /* parity = */UsbSerialPort.PARITY_NONE,
            )

            action.invoke(port)
        }.recoverCatching {
            Timber.e(it, "badge: Couldn't perform actions with port ${port.portNumber}.")
            // Just send a generic exception with a message we want
            throw RuntimeException("Failed to perform actions")
        }.also {
            if (port.isOpen) {
                port.close()
            }
            connection.close()
        }

    }

    private fun sendCommandToBadge(
        command: String,
    ) = performReturnableActionOnBadge { port ->
        port.write(command.toByteArray(), READ_WRITE_TIMEOUT)
        Timber.i("badge: Wrote '$command' to port ${port.portNumber}.")
        command.length
    }

    private fun receiveResponseFromBadge() = performReturnableActionOnBadge { port ->
        val bytes = ByteArray(READ_BYTE_ARRAY_SIZE)
        val count = port.read(bytes, READ_WRITE_TIMEOUT)

        Timber.i("badge: Read '$count' bytes from port ${port.portNumber}.")
        String(bytes)

    }

    private fun informNoBadgeFound(manager: UsbManager): Result<Nothing> {
        val message = "Could not find usb device with product name '$DEVICE_PRODUCT_NAME'.\nFound product(s):\n${
            manager.connectedProductNames().joinToString("\n •")
        }"

        Timber.e("Badge Connection", message)

        return Result.failure(RuntimeException(message))
    }

    private suspend fun askPermission(
        device: UsbDevice,
    ): Unit = suspendCancellableCoroutine { continuation ->
        class BoundUsbReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (ACTION_USB_PERMISSION == intent.action && continuation.isActive) {
                    val boundDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (boundDevice != null) {
                            Timber.d("USB Permission: Permission granted.")
                            continuation.resume(Unit)
                        } else {
                            continuation.resumeWithException(RuntimeException("No bound device"))
                        }
                    } else {
                        Timber.e("USB Permission: Could not request permission to access to badge.")
                    }
                }
            }
        }

        val permissionIntent = PendingIntent.getBroadcast(
            /* context = */ context,
            /* requestCode = */ ACTION_USB_PERMISSION_REQUEST_CODE,
            /* intent = */ Intent(ACTION_USB_PERMISSION),
            /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        val broadcastReceiver = BoundUsbReceiver()
        context.registerReceiver(
            broadcastReceiver,
            filter,
            Context.RECEIVER_NOT_EXPORTED,
        )

        manager.requestPermission(device, permissionIntent)

        continuation.invokeOnCancellation {
            context.unregisterReceiver(broadcastReceiver)
        }
    }


    private fun UsbManager.findConnectedBadge() =
        deviceList.values.firstOrNull {
            it.productName == DEVICE_PRODUCT_NAME
        }

    private fun UsbManager.connectedProductNames() =
        deviceList.values.map { it.productName ?: "<none>" }

}

actual data class Environment(val context: Context)

actual fun buildBadgeManager(
    environment: Environment,
): BadgeManager = AndroidBadgeManager(environment.context)
