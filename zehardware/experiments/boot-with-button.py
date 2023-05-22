import usb_cdc
import storage

# enable cdc serial for communication
usb_cdc.enable(console=True, data=True)

# check if we want to program or badge?

from digitalio import DigitalInOut, Direction
derp = digitalio.DigitalInOut(board.SW_A)
derp.direction = digitalio.Direction.INPUT
derp.pull = digitalio.Pull.DOWN

storage.remount("/", readonly=derp.value)

print(f"booted as readonly={derp.value}")
