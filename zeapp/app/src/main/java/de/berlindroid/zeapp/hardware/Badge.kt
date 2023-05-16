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
import de.berlindroid.zeapp.bits.toBinary
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Base64
import java.util.zip.ZipOutputStream


private const val ACTION_USB_PERMISSION = "ACTION_USB_PERMISSION"
private const val DEVICE_PRODUCT_NAME = "Badger 2040"
private const val ACTION_USB_PERMISSION_REQUEST_CODE = 4711

class Badge {
    data class Payload(
        val type: String,
        val meta: String,
        val payload: String,
    ) {
        fun toBadgeCommand(): String = "$type:$meta:$payload".encodeToByteArray().base64()
    }

    data class FoundUSBConnectionParameters(
        val usbInterface: UsbInterface,
        val usbEndpoint: UsbEndpoint,
    )

    var lastResult: Int = 0

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
                    val data = "${payload.toBadgeCommand()}\n"
                    val bulkData = data.toByteArray()

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
                        } else {
                            Log.e("Badge Connection", "Failed with result $result.")
                        }
                    } catch (th: Throwable) {
                        Log.e("Badge Connection", "Error while sending.", th)
                    } finally {
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
            PendingIntent.FLAG_UPDATE_CURRENT,
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
    var foundInterface: UsbInterface? = null
    var foundEndpoint: UsbEndpoint? = null

    for (interfaceIndex in 0 until interfaceCount) {
        val interf = getInterface(interfaceIndex)
        for (endpointIndex in 0 until interf.endpointCount) {
            val endpoint = interf.getEndpoint(endpointIndex)

            if (endpoint.isCompatible()) {
                foundInterface = interf
                foundEndpoint = endpoint
                break;
            }
        }

        if (foundEndpoint != null) {
            break
        }
    }

    return if (foundInterface != null && foundEndpoint != null) {
        Badge.FoundUSBConnectionParameters(foundInterface, foundEndpoint)
    } else {
        null
    }
}

private fun UsbEndpoint.isCompatible(): Boolean = type == UsbConstants.USB_ENDPOINT_XFER_BULK

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

fun ByteArray.base64(): String = Base64.getEncoder().encodeToString(this)
fun String.debase64(): ByteArray = Base64.getDecoder().decode(this)
