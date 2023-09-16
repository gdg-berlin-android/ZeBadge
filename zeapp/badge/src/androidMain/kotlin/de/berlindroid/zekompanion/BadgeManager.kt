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
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidBadgeManager(
    private val context: Context,
) : BadgeManager {

    companion object {
        const val ACTION_USB_PERMISSION = "ACTION_USB_PERMISSION"
        const val ACTION_USB_PERMISSION_REQUEST_CODE = 4711
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

    override fun isConnected(): Boolean = manager.findConnectedBadge() != null

    private fun sendCommandToBadge(
        command: String,
    ): Result<Int> {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)

        val driver = availableDrivers.first()
        val port = driver.ports.last()
        val connection = manager.openDevice(driver.device)
        return kotlin.runCatching {
            port.open(connection)
            port.dtr = true
            port.setParameters(
                /* baudRate = */115200,
                /* dataBits = */UsbSerialPort.DATABITS_8,
                /* stopBits = */UsbSerialPort.STOPBITS_1,
                /* parity = */UsbSerialPort.PARITY_NONE,
            )
            port.write(command.toByteArray(), 3_000)
            Timber.i("badge", "Wrote '$command' to port ${port.portNumber}.")

            command.length
        }.recoverCatching {
            Timber.e("badge", "Couldn't write to port ${port.portNumber}.", it)
            // Just send a generic exception with a message we want
            throw RuntimeException("Failed to write")
        }.also {
            if (port.isOpen) {
                port.close()
            }
            connection.close()
        }
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
                            Timber.d("USB Permission", "Permission granted.")
                            continuation.resume(Unit)
                        } else {
                            continuation.resumeWithException(RuntimeException("No bound device"))
                        }
                    } else {
                        Timber.e("USB Permission", "Could not request permission to access to badge.")
                        continuation.resumeWithException(
                            RuntimeException("Could not request permission to access to badge."),
                        )
                    }
                }
            }
        }

        val permissionIntent = PendingIntent.getBroadcast(
            context,
            ACTION_USB_PERMISSION_REQUEST_CODE,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        val broadcastReceiver = BoundUsbReceiver()
        context.registerReceiver(
            broadcastReceiver,
            filter,
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
