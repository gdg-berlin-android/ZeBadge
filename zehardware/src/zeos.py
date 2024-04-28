import board
import serial
import time
import traceback
import ui

from message import Message

from app_fetch import FetchApp
from app_store_and_show import StoreAndShowApp

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
        self.active_app = None

        self.led = DigitalInOut(board.USER_LED)
        self.led.direction = Direction.OUTPUT
        self.led.value = 0
        self.led_on = False

        self.buttons = SystemButtons()
        self.tasks.append(_update_system_buttons)

        # add defaults
        self._reset_subscribers()
        self._init_interfaces()
        self._init_applications()

        self.system_subscribers = self.subscribers.copy()

    def _reset_subscribers(self):
        self.subscribers.clear()

        # add default subscriptions
        self.subscribe('info', _info_handler)
        self.subscribe('error', _error_handler)
        self.subscribe('reload', _reload_handler)
        self.subscribe('exit', _exit_handler)

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
        for task in enumerate(self.tasks):
            print(task)

        while True:
            try:
                for task in self.tasks:
                    print(f".", end="")
                    task(self)

                print(':', end='')

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
        # init always on tasks
        ui.init(self)
        serial.init(self)

        # check keyboard and things
        i2c = board.I2C()
        while not i2c.try_lock():
            continue

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
            if not wifi.init(self):
                print("... no wifi found.")

    def _init_applications(self):
        store_and_show = StoreAndShowApp(self)

        store_and_show.run()
        self.active_app = store_and_show

        self.subscribe('system_button_c_released', lambda os, message: ui._show_terminal_handler(os, message))


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
            if pressed:
                state = "pressed"
            else:
                state = "released"

            os.messages.append(Message(f'system_button_{button}_{state}', (button, pressed)))


def _error_handler(os, message):
    print(f"\033[35mError: {message.value}\033[m")


def _info_handler(os, message):
    print(f"\033[32mInfo: {message.value}\033[m")


def _reload_handler(os, message):
    import time
    import supervisor

    time.sleep(0.5)
    supervisor.reload()


def _exit_handler(os, message):
    import time
    import sys

    time.sleep(0.5)
    sys.exit()
