#!/bin/python
#
# Use me instead of code.py on the XIAO
#
# It will enable the light on d1 if 'light on' gets sent to ttyACM1 (data)
#
# > echo 'light on' > /dev/ttyACM1
#

import time
import board
from digitalio import DigitalInOut, Direction, Pull
from adafruit_hid.keyboard import Keyboard
from adafruit_hid.keycode import Keycode
import usb_hid
import usb_cdc

kbd = Keyboard(usb_hid.devices)

# d0 = DigitalInOut(board.D0)
# d0.direction = Direction.INPUT
# d0.pull = Pull.DOWN

# d1 = DigitalInOut(board.D1)
# d1.direction = Direction.OUTPUT

# d1.value = False

led = DigitalInOut(board.USER_LED)
led.direction = Direction.OUTPUT

plus = lambda x, y:  x+ y

def parse_line(line):
    line = line.strip()
    print("got line : >%s<" % line)

    if not line.startswith('::'):
        return None

    token, first, operator, second = line.split()
    if operator == "+":
        return plus(int(first),int(second))
    else:
        return None

counter = 0
while(True):
    #if d0.value is True:
    #    kbd.send(Keycode.A)
    #    led.value = False
    #    time.sleep(3)

    if counter % 10 == 0:
        print(".. still alive ..")

    counter += 1

    if usb_cdc.data.in_waiting > 0:
        print("... reading")
        line = str(usb_cdc.data.readline(), 'utf-8').strip()

        print("... got %s" % line)
        if line == "light on":
            print("... 💡")
            #d1.value = True
            led.value = True
        elif line == "light off":
            print("... 🌑")
            #d1.value = False
            led.value = False
        else:
            print("... kaput ..")

    led.value = True
    time.sleep(0.3)
    led.value = False
    time.sleep(0.3)
