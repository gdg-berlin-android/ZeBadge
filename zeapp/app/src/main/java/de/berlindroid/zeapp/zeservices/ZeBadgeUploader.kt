package de.berlindroid.zeapp.zeservices

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import dagger.hilt.android.qualifiers.ApplicationContext
import de.berlindroid.zeapp.zebits.toBinary
import de.berlindroid.zeapp.zemodels.ZeBadgePayload
import de.berlindroid.zeapp.zebits.base64
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.RuntimeException
import java.util.zip.Deflater
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ZeBadgeUploader @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val ACTION_USB_PERMISSION = "ACTION_USB_PERMISSION"
        private const val DEVICE_PRODUCT_NAME = "Badger 2040"
        private const val ACTION_USB_PERMISSION_REQUEST_CODE = 4711
    }

    class BadgeUploadException(message: String) : RuntimeException(message)

    /**
     * Send a bitmap to the badge for the name slot
     *
     * @param name the name of the page / slot for the bitmap
     * @param page the bitmap in black / white to be send to the badge
     */
    suspend fun sendPage(name: String, page: Bitmap): Result<Int> {
        val payload = ZeBadgePayload(
            type = "preview",
            meta = "",
            payload = page.toBinary().zipit().base64()
        )

        return sendToUsb(payload)
    }

    private suspend fun sendToUsb(payload: ZeBadgePayload): Result<Int> {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = manager.findConnectedBadge()

        return if (device == null) {
            informNoBadgeFound(manager)
        } else {
            if (!manager.hasPermission(device)) {
                askPermission(manager, device)
            }

            val actualCommand = payload.toBadgeCommand()
            sendCommandToBadge(
                manager,
                actualCommand
            )
        }
    }

    private fun sendCommandToBadge(
        manager: UsbManager,
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
            Log.i("badge", "Wrote '$command' to port ${port.portNumber}.")

            command.length
        }.recoverCatching {
            Log.e("badge", "Couldn't write to port ${port.portNumber}.", it)
            // Just send a generic exception with a message we want
            throw BadgeUploadException("Failed to write")
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

        Log.e("Badge Connection", message)

        return Result.failure(BadgeUploadException(message))
    }

    private suspend fun askPermission(
        manager: UsbManager,
        device: UsbDevice,
    ): Unit = suspendCancellableCoroutine { continuation ->
        class BoundUsbReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (ACTION_USB_PERMISSION == intent.action) {
                    val boundDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (boundDevice != null) {
                            Log.d("USB Permission", "Permission granted.")
                            continuation.resume(Unit)
                        } else {
                            continuation.resumeWithException(BadgeUploadException("No bound device"))
                        }
                    } else {
                        Log.e("USB Permission", "Could not request permission to access to badge.")
                        continuation.resumeWithException(
                            BadgeUploadException("Could not request permission to access to badge.")
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
            filter
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

    /**
     * Compress a given byte array to a smaller byte array.
     */
    fun ByteArray.zipit(): ByteArray {
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.reset()
        deflater.setInput(this)
        deflater.finish()

        var result = ByteArray(0)
        val o = ByteArrayOutputStream(1)
        try {
            val buf = ByteArray(64)
            var got = 0
            while (!deflater.finished()) {
                got = deflater.deflate(buf)
                o.write(buf, 0, got)
            }
            result = o.toByteArray()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                o.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            deflater.end()
        }
        return result
    }
}