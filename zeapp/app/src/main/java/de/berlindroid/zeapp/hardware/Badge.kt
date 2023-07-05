package de.berlindroid.zeapp.hardware

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort.DATABITS_8
import com.hoho.android.usbserial.driver.UsbSerialPort.PARITY_NONE
import com.hoho.android.usbserial.driver.UsbSerialPort.STOPBITS_1
import com.hoho.android.usbserial.driver.UsbSerialProber
import de.berlindroid.zeapp.zebits.toBinary
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Base64
import java.util.zip.Deflater


private const val ACTION_USB_PERMISSION = "ACTION_USB_PERMISSION"
private const val DEVICE_PRODUCT_NAME = "Badger 2040"
private const val ACTION_USB_PERMISSION_REQUEST_CODE = 4711

/**
 * Hardware communication abstraction.
 */
class Badge(
    val success: (sentBytes: Int) -> Unit,
    val failure: (error: String) -> Unit,
) {
    /**
     * What to be send over to the badge?
     *
     * @param type what command should be executed
     * @param meta any meta information you want to receive back?
     * @param payload the payload of the command of type 'type'.
     */
    data class Payload(
        val debug: Boolean = false,
        val type: String,
        val meta: String,
        val payload: String,
    ) {
        /**
         * Convert the payload to a format the badge understands
         */
        fun toBadgeCommand(): String = "${if (debug) "debug:" else ""}$type:$meta:$payload"

    }

    /**
     * Send a bitmap to the badge for the name slot
     *
     * @param context Android context to communicate with the usb interface
     * @param name the name of the page / slot for the bitmap
     * @param page the bitmap in black / white to be send to the badge
     */
    fun sendPage(context: Context, name: String, page: Bitmap) {
        val payload = Payload(
            type = "preview",
            meta = "",
            payload = page.toBinary().zipit().base64()
        )

        sendToUsb(context, payload)
    }

    private fun sendToUsb(context: Context, payload: Payload) {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = manager.findConnectedBadge()

        if (device == null) {
            informNoBadgeFound(manager, context)
        } else {
            if (!manager.hasPermission(device)) {
                askPermissionAndRetry(context, manager, device, payload)
            } else {
                val actualCommand = payload.toBadgeCommand()
                sendCommandToBadge(
                    manager,
                    actualCommand
                )
            }
        }
    }

    private fun sendCommandToBadge(
        manager: UsbManager,
        command: String,
    ) {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)

        val driver = availableDrivers.first()
        val port = driver.ports.last()
        val connection = manager.openDevice(driver.device)
        try {
            port.open(connection)
            port.dtr = true
            port.setParameters(
                /* baudRate = */115200,
                /* dataBits = */DATABITS_8,
                /* stopBits = */STOPBITS_1,
                /* parity = */PARITY_NONE,
            )
            port.write(command.toByteArray(), 3_000)
            Log.i("badge", "Wrote '$command' to port ${port.portNumber}.")

            success(command.length)
        } catch (e: Exception) {
            Log.e("badge", "Couldn't write to port ${port.portNumber}.", e)
            failure("Couldn't write")
        } finally {
            if (port.isOpen) {
                port.close()
            }
            connection.close()
        }
    }

    private fun informNoBadgeFound(manager: UsbManager, context: Context) {
        val message = "Could not find usb device with product name '${
            DEVICE_PRODUCT_NAME
        }'.\nFound product(s):\n${
            manager.connectedProductNames().joinToString("\n •")
        }"

        Log.e("Badge Connection", message)

        failure(message)
    }

    private fun askPermissionAndRetry(
        context: Context,
        manager: UsbManager,
        device: UsbDevice,
        payload: Payload
    ) {
        class BoundUsbReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (ACTION_USB_PERMISSION == intent.action) {
                    val boundDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        boundDevice?.apply {
                            Log.d("USB Permission", "Permission granted.")

                            sendToUsb(context, payload)
                        }
                    } else {
                        Log.e("USB Permission", "Could not request permission to access to badge.")
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
        context.registerReceiver(
            BoundUsbReceiver(),
            filter
        )

        manager.requestPermission(device, permissionIntent)
    }
}

private fun UsbManager.findConnectedBadge() =
    deviceList.values.firstOrNull {
        it.productName == DEVICE_PRODUCT_NAME
    }

private fun UsbManager.connectedProductNames() =
    deviceList.values.map { it.productName ?: "<none>" }

/**
 * Helper to convert a bytearray to base64
 */
fun ByteArray.base64(): String = Base64.getEncoder().encodeToString(this)

/**
 * Take a base64 encoded string and convert it back to å bytearray.
 */
fun String.debase64(): ByteArray = Base64.getDecoder().decode(this)

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
