import usb_cdc
import storage

usb_cdc.enable(console=True, data=True)
storage.remount("/", readonly=False)
