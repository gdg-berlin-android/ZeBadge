#!/bin/python
#
# To properly send commands, you must end them with CR+LF.
# The easiest way is apparently using Pico COM, like below.
# (that will map CR to CR+LF on each echo)
#
# Linux: picocom /dev/ttyACM1 --omap crcrlf --echo
# Mac: picocom /dev/cu.usbmodem2103 --omap crcrlf --echo
# Windows: Change OS
#
# Thonny's interactive mode (REPL) should also work.

import re
import sys
import time
import board
import supervisor
import circuitpython_base64 as base64
from digitalio import DigitalInOut, Direction


# Configuration

READ_TIMEOUT =    2 # seconds
LOOP_CYCLE   =  0.3 # seconds
KEEP_ALIVE   =    5 # seconds
DEBUG        = True

def log(string):
    if DEBUG: print(string)


## Run board setup
led = DigitalInOut(board.USER_LED)
led.direction = Direction.OUTPUT
log("-----")
log("Running in serial mode.")


# Keep alive pinger
iteration = 0
def log_keep_alive():
    global iteration
    keep_alive_cycle = int(KEEP_ALIVE / LOOP_CYCLE)
    if iteration % keep_alive_cycle == 0:
        time_string = "{:0>2}".format(time.localtime().tm_min)
        time_string += ":{:0>2}".format(time.localtime().tm_sec)
        log("Awaiting commands… (%s)" % time_string)


# Blink it when doing some work
should_blink_led = False
def handle_blink():
    global led
    if should_blink_led:
        led.value = not led.value
    else:
        led.value = False


# Read a single command from the serial interface
def read_command():
    buffer = ""
    if not supervisor.runtime.usb_connected:
        log("No USB connection, skipping read")
        return None
    if not supervisor.runtime.serial_connected:
        log("No serial connection, skipping read")
        return None
    while supervisor.runtime.serial_bytes_available:
        buffer += sys.stdin.read(1)
    cleaned = re.sub(r'\s', " ", buffer).strip()
    return cleaned if len(cleaned) > 0 else None


# Base64<command:metadata:payload> -> [command, metadata, payload]
# For debug, you can skip Base64 and put "debug:" in front
COMMAND_NONE = [None, None, None]
def parse_command(base64_string):
    if base64_string == None:
        return COMMAND_NONE
    if DEBUG and base64_string.startswith("debug:"):
        debug_command = base64_string.replace("debug:", "")
        parts = debug_command.split(":")
        if len(parts) != 3:
            log("Invalid debug command: '%s'" % debug_command)
            log("  - Did you forget to add colons?")
            return COMMAND_NONE
        return parts
    try:
        base64_bytes = base64_string.encode("utf-8")
        bytes_plain = base64.decodebytes(base64_bytes)
        plain_string = str(bytes_plain, "utf-8")
        parts = plain_string.split(":")
        if len(parts) != 3:
            raise Exception("Invalid command format: '%s'" % plain_string)
        return plain_string.split(":")
    except Exception as e:
        message = str(e) if len(str(e)) > 0 else "🤷"
        log(f"Failed to decode '{base64_string}'. Reason: %s" % message)
        return COMMAND_NONE


# Handle commands in format Base64<command:metadata:payload>
def handle_commands():
    global should_blink_led
    command_raw = read_command()
    command, metadata, payload = parse_command(command_raw)
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
