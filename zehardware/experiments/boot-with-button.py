# check if we want to program or badge?
import digitalio
import board

button = digitalio.DigitalInOut(board.SW_A)
button.direction = digitalio.Direction.INPUT
button.pull = digitalio.Pull.DOWN

import usb_cdc
import storage

# enable cdc serial for communication
usb_cdc.enable(console=True, data=True)

storage.remount("/", readonly=button.value)

print(f"booted as readonly={button.value}")
