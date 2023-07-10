import usb_cdc
import usb_hid
import storage

usb_cdc.enable(console=True, data=True)
storage.remount("/", readonly=False)
# Don't emulate keyboard when plugged in
usb_hid.disable()
