package de.berlindroid.zeapp.hardware

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log
import android.widget.Toast
import de.berlindroid.zeapp.bits.toBinary
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Base64
import java.util.zip.ZipOutputStream


private const val ACTION_USB_PERMISSION = "ACTION_USB_PERMISSION"
private const val DEVICE_PRODUCT_NAME = "Badger 2040"
private const val ACTION_USB_PERMISSION_REQUEST_CODE = 4711

/**
 * Hardware communication abstraction.
 */
class Badge {
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
        fun toBadgeCommand(): String =
            "${
                if (debug) "debug:" else ""
            }$type:$meta:$payload"
                .encodeToByteArray()
                .base64()
    }

    /**
     * Parameters of a connection, that suits our needs.

     * @param usbInterface the interface found and happy
     * @param usbEndpoint the endpoint to be used for communication
     */
    data class FoundUSBConnectionParameters(
        val usbInterface: UsbInterface,
        val usbEndpoint: UsbEndpoint,
    )

    /**
     * last result of communication with the badge
     */
    var lastResult: Int = 0

    /**
     * Send a bitmap to the badge for the name slot
     *
     * @param context Android context to communicate with the usb interface
     * @param name the name of the page / slot for the bitmap
     * @param page the bitmap in black / white to be send to the badge
     */
    fun sendPage(context: Context, name: String, page: Bitmap) {
        val payload = Payload(
            type = "blink",
            meta = "",
            payload = page.toBinary().base64()
        )

        sendToUsb(context, payload)
    }

    private fun sendToUsb(context: Context, payload: Payload) {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = manager.findConnectedBadge()
        if (device == null) {
            Log.e(
                "Badge Connection",
                "Could not find usb device with product name '${
                    DEVICE_PRODUCT_NAME
                }'.\nFound product(s):\n${
                    manager.connectedProductNames().joinToString("\n •")
                }"
            )
        } else {
            if (!manager.hasPermission(device)) {
                askPermissionAndRetry(context, manager, device, payload)
            } else {
                val parameter = device.findParameter()
                if (parameter != null) {
                    val (iface, endpoint) = parameter
//                    val data = "${payload.toBadgeCommand()}\n"
                    val data = "farts\n"
                    val bulkData = data.toByteArray(Charsets.UTF_8)

                    val connection = manager.openDevice(device)
                    try {
                        connection.claimInterface(iface, true)
                        val result = connection.bulkTransfer(
                            endpoint,
                            bulkData,
                            bulkData.size,
                            3_000
                        )
                        connection.releaseInterface(iface)
                        lastResult = result

                        if (result > 0) {
                            Log.d("Badge Connection", "Success")
                            Toast.makeText(
                                context,
                                "Sent '$result' bytes successfully.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Log.e("Badge Connection", "Failed with result $result.")
                            Toast.makeText(
                                context,
                                "Couldn't send '${String(bulkData)}' to '$iface' and '$endpoint'.\n\nResult was '$result'.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (th: Throwable) {
                        Log.e("Badge Connection", "Error while sending.", th)
                    } finally {
                        connection.releaseInterface(iface)
                        connection.close()
                    }
                }
            }
        }
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
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
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

private fun UsbDevice.findParameter(): Badge.FoundUSBConnectionParameters? {
    val foundInterfaces = mutableListOf<UsbInterface>()
    val foundEndpoints = mutableListOf<UsbEndpoint>()

    for (interfaceIndex in 0 until interfaceCount) {
        val interf = getInterface(interfaceIndex)

        if (interf.name.orEmpty().contains("CDC2 data")) {
            for (endpointIndex in 0 until interf.endpointCount) {
                val endpoint = interf.getEndpoint(endpointIndex)

                if (endpoint.isCompatible()) {
                    foundInterfaces.add(interf)
                    foundEndpoints.add(endpoint)
                }
            }
        }
    }

    return if (foundInterfaces.isNotEmpty() && foundEndpoints.isNotEmpty()) {
        Badge.FoundUSBConnectionParameters(foundInterfaces.first(), foundEndpoints.first())
    } else {
        null
    }
}

private fun UsbEndpoint.isCompatible(): Boolean = direction == UsbConstants.USB_DIR_OUT

private fun ByteArray.zipit(): ByteArray {
    val output = ByteArrayOutputStream()
    try {
        ZipOutputStream(output).use { zip ->
            zip.write(this)
            zip.close()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return output.toByteArray()
}

/**
 * Helper to convert a bytearray to base64
 */
fun ByteArray.base64(): String = Base64.getEncoder().encodeToString(this)

/**
 * Take a base64 encoded string and convert it back to å bytearray.
 */
fun String.debase64(): ByteArray = Base64.getDecoder().decode(this)
