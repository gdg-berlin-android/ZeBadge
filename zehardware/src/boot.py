# check if we want to program
import digitalio
import board
import usb_cdc
import storage

button = digitalio.DigitalInOut(board.SW_A)
button.direction = digitalio.Direction.INPUT
button.pull = digitalio.Pull.DOWN

dev_mode = button.value

# enable cdc serial for communication
usb_cdc.enable(console=dev_mode, data=True)
storage.remount("/", readonly=dev_mode)

print(" ++ ZeBadgeOS 2024 ++ ")

if dev_mode:
    print("Welcome, fellow developer!")
else:
    print(" ~ UserMode enabled ~ ")

try:
    if board.DISPLAY.is_zebadge():
        print(".. running ZePython..")
    else:
        print(".. ZePython on non ZeBadge ..")
except AttributeError:
    print(".. not on ZePython ..")

