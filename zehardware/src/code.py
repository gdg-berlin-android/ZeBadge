from zeos import ZeBadgeOs
from util import exception_to_readable
import digitalio
import board

button = digitalio.DigitalInOut(board.SW_C)
button.direction = digitalio.Direction.INPUT
button.pull = digitalio.Pull.DOWN
safe_mode = button.value
button.deinit()
del button

if safe_mode:
    del safe_mode
    while True:
        print("SafeMode", end="...")
else:
    badgeos = ZeBadgeOs()
    while True:
        try:
            badgeos.run()
        except Exception as e:
            print(exception_to_readable(e))
