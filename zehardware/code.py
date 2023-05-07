#!/bin/python
#
# To properly send commands, you must end them with CR+LF.
# The easiest way is apparently using Pico COM, like below.
# (that will map CR to CR+LF on each echo)
#
# Linux: picocom /dev/ttyACM1 --omap crcrlf --echo
# Mac: picocom /dev/cu.usbmodem2103 --omap crcrlf --echo
# Windows: Change OS

import re
import sys
import time
import board
import supervisor
from digitalio import DigitalInOut, Direction, Pull


# Configuration

READ_TIMEOUT =    2 # seconds
LOOP_CYCLE   =  0.3 # seconds
KEEP_ALIVE   =    5 # seconds
DEBUG        = True

def log(string):
    if DEBUG: print(string)


## Prepare the board

led = DigitalInOut(board.USER_LED)
led.direction = Direction.OUTPUT

log("-----")
log("Running in serial mode.")


# Keep alive logger
iteration = 0
def log_keep_alive():
    global iteration
    keep_alive_cycle = int(KEEP_ALIVE / LOOP_CYCLE)
    if iteration % keep_alive_cycle == 0:
        log("Awaiting commands…")


# Blink it when doing some work
should_blink_led = False
def handle_blink():
    global led
    if should_blink_led:
        led.value = not led.value
    else:
        led.value = False


# Read a single command from the serial interface
read_buffer = ""
def read_command():
    global read_buffer
    if not supervisor.runtime.usb_connected:
        log("No USB connection, skipping read")
        return None
    if not supervisor.runtime.serial_connected:
        log("No serial connection, skipping read")
        return None
    while supervisor.runtime.serial_bytes_available:
        read_buffer += sys.stdin.read(1)
    if read_buffer.endswith("\n") or read_buffer.endswith("\r"):
        cleaned = re.sub(r'\s', "", read_buffer).strip()
        if len(cleaned) > 0:
            read_buffer = ""
            return cleaned
    return None


# Handle commands in format Base64<command:metadata:content>
def handle_commands():
    global should_blink_led
    command = read_command()
    if command == "blink":
        log("Changing blink status…")
        should_blink_led = not should_blink_led
    elif command == "reload":
        log("Reloading…")
        time.sleep(1)
        supervisor.reload()
    elif command == "exit":
        sys.exit()
    elif command != None:
        log("Unknown command '%s'" % command)


### The Main Loop ###

while True:
    time.sleep(LOOP_CYCLE)
    log_keep_alive()
    handle_blink()
    handle_commands()
    iteration += 1
