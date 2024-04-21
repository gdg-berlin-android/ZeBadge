import board
import serial
import time
import traceback
import ui

from message import Message

from app_fetch import FetchApp

from digitalio import DigitalInOut
from digitalio import Direction
from digitalio import Pull


class ZeBadgeOs:
    # the os of the badge, coordinating tasks to be run, external hardware
    # and events sent between.
    #
    # is it overengineered, yes.

    def __init__(self):
        # create the os

        # fields
        self.tasks = []
        self.subscribers = {}
        self.messages = []

        self.led = DigitalInOut(board.USER_LED)
        self.led.direction = Direction.OUTPUT
        self.led.value = 0
        self.led_on = False

        self.buttons = SystemButtons()

        # add default tasks
        self._init_interfaces()

        self.tasks.append(_update_system_buttons)
        self.tasks.append(serial.read_input)

        # add default subscriptions
        self.subscribe('info', _info_handler)
        self.subscribe('error', _error_handler)
        self.subscribe('system_buttons_changed', _system_button_handler)

        self.add_applications()

    def subscribe(self, topic: str, subscriber):
        # subscribe a lambda to a topic

        if topic not in self.subscribers:
            self.subscribers[topic] = []

        self.subscribers[topic].append(subscriber)

    def unsubscribe(self, topic: str):
        # unsubscribe from a topic

        if topic in self.subscribers:
            self.subscribers[topic] = []
        else:
            print(f"'{topic}' does not exist.")

    def run(self):
        # start os, never returning unless exception wasn't caught

        while True:
            try:
                for task in self.tasks:
                    task(self)

                print('.', end='')

                current_messages = self.messages.copy()
                self.messages.clear()

                for message in current_messages:
                    if message.topic in self.subscribers:
                        for subscriber in self.subscribers[message.topic]:
                            subscriber(self, message)
                    else:
                        self.messages.append(Message(
                            "error",
                            f"no subscriber for {message.topic}: {message.value}"
                        ))

                self.led_on = not self.led_on
                self.led.value = self.led_on

            except Exception as e:
                traceback.print_exception(e)

            time.sleep(0.2)

    def _init_interfaces(self):
        ui.init(self)

        # check keyboard and things
        i2c = board.I2C()
        while not i2c.try_lock():
            pass

        addrs = i2c.scan()
        i2c.unlock()

        print(f"init: i2c interfaces: {addrs}")

        if addrs:
            has_keyboard = 95 in addrs
            if has_keyboard:
                import keyboard
                keyboard.init(self)
        else:
            print("... no i2c found, trying wifi")
            import wifi
            wifi.init(self)

    def add_applications(self):
        fetch = FetchApp(self)

        def check_for_c(_, message):
            button, pressed = message.value
            if button == 'c' and pressed:
                fetch.run()

        self.subscribe('system_buttons_changed', check_for_c)


class SystemButtons:
    def __init__(self):
        self.a = _system_button(board.SW_A)
        self.b = _system_button(board.SW_B)
        self.c = _system_button(board.SW_C)
        self.up = _system_button(board.SW_UP)
        self.down = _system_button(board.SW_DOWN)
        self.last = self.snapshot()

    def snapshot(self):
        return {
            'a': self.a.value,
            'b': self.b.value,
            'c': self.c.value,
            'up': self.up.value,
            'down': self.down.value,
        }

    def changes(self):
        result = {}
        current = self.snapshot()
        if current['a'] != self.last['a']: result['a'] = current['a']
        if current['b'] != self.last['b']: result['b'] = current['b']
        if current['c'] != self.last['c']: result['c'] = current['c']
        if current['up'] != self.last['up']: result['up'] = current['up']
        if current['down'] != self.last['down']: result['down'] = current['down']

        self.last = current
        return result


def _system_button(pin) -> DigitalInOut:
    button = DigitalInOut(pin)
    button.direction = Direction.INPUT
    button.pull = Pull.DOWN
    return button


def _update_system_buttons(os):
    changes = os.buttons.changes()

    if len(changes) > 0:
        for button in changes:
            pressed = changes[button]
            os.messages.append(Message('system_buttons_changed', (button, pressed)))


def _system_button_handler(os, message):
    button, pressed = message.value
    if pressed:
        state = 'pressed'
    else:
        state = 'released'

    print(f"button updated: '{button}' is now '{state}'")


def _error_handler(os, event):
    print(f"\033[35mError: {event.value}\033[m")


def _info_handler(os, event):
    print(f"\033[32mInfo: {event.value}\033[m")
