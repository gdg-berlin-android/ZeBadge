import board
import gc
import serial
import sys
import time
import traceback
import ui
import usb_cdc

import os as systemos

from digitalio import DigitalInOut
from digitalio import Direction
from digitalio import Pull

from message import Message
from config import save_config
from config import load_config
from config import update_config
from config import Configuration
from app_fetch import FetchApp
from app_store_and_show import StoreAndShowApp


class MessageKey:
    INFO = "info"
    ERROR = "error"
    RELOAD = "reload"
    EXIT = "exit"
    TICK = "tick"
    CONFIG_LOAD = "config_load"
    CONFIG_SAVE = "config_save"
    CONFIG_UPDATE = "config_update"
    CONFIG_LIST = "config_list"
    BUTTON_A_RELEASED = "BTN_A_RELEASED"
    BUTTON_A_PRESSED = "BTN_A_PRESSED"
    BUTTON_B_RELEASED = "BTN_B_RELEASED"
    BUTTON_B_PRESSED = "BTN_B_PRESSED"
    BUTTON_C_RELEASED = "BTN_C_RELEASED"
    BUTTON_C_PRESSED = "BTN_C_PRESSED"
    BUTTON_UP_RELEASED = "BTN_UP_RELEASED"
    BUTTON_UP_PRESSED = "BTN_UP_PRESSED"
    BUTTON_DOWN_RELEASED = "BTN_DOWN_RELEASED"
    BUTTON_DOWN_PRESSED = "BTN_DOWN_PRESSED"
    BUTTON_DEVELOPER_RELEASED = "BTN_DEVELOPER_RELEASED"
    BUTTON_DEVELOPER_PRESSED = "BTN_DEVELOPER_PRESSED"
    BUTTON_CHANGED = "BTN_CHANGED"


class ZeBadgeOs:
    # the os of the badge, coordinating tasks to be run, external hardware
    # and events sent between.
    #
    # is it overengineered, yes.

    next_subscription_id = 0

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
        self.config = Configuration()
        load_config(self.config)

        self._reset_subscribers()
        self._init_interfaces()
        self._init_applications()

        self.system_subscribers = self.subscribers.copy()

    def _reset_subscribers(self):
        self.subscribers.clear()

        # add default subscriptions
        self.subscribe(serial.MessageKey.RECEIVED, _serial_received_handler)

        self.subscribe(MessageKey.INFO, _info_handler)
        self.subscribe(MessageKey.ERROR, _error_handler)
        self.subscribe(MessageKey.RELOAD, _reload_handler)
        self.subscribe(MessageKey.EXIT, _exit_handler)

        self.subscribe(MessageKey.CONFIG_LOAD, lambda os, message: load_config(self.config))
        self.subscribe(MessageKey.CONFIG_SAVE, lambda os, message: save_config(self.config))
        self.subscribe(MessageKey.CONFIG_UPDATE, lambda os, message: update_config(self.config, message.value))
        self.subscribe(MessageKey.CONFIG_LIST,
                       lambda os, message: self.messages.append(
                           Message(serial.MessageKey.RESPOND, str(self.config)))
                       )

    def subscribe(self, topic: str, subscriber) -> int:
        # subscribe a callback to a topic. Return an id for deletion.

        if topic not in self.subscribers:
            self.subscribers[topic] = {}

        subscription_id = ZeBadgeOs.next_subscription_id
        ZeBadgeOs.next_subscription_id += 1

        self.subscribers[topic][subscription_id] = subscriber
        return subscription_id

    def unsubscribe(self, subscription_id):
        found = None

        for topic in self.subscribers:
            if subscription_id in self.subscribers[topic]:
                found = self.subscribers[topic][subscription_id]
                del self.subscribers[topic][subscription_id]

            if len(self.subscribers[topic]) == 0:
                del self.subscribers[topic]

        return found is not None

    def get_stored_files(self):
        return list(filter(lambda x: x.endswith('b64'), systemos.listdir('/')))

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
                current_messages += [Message(MessageKey.TICK, None)]
                self.messages.clear()

                for message in current_messages:
                    if message.topic in self.subscribers:
                        subscriber_ids = self.subscribers[message.topic]
                        for subscriber_id in subscriber_ids:
                            subscriber = subscriber_ids[subscriber_id]
                            subscriber(self, message)
                    else:
                        print(f'no {message.topic} topic. ', end='')

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

            self.config.keyboard_attached = has_keyboard
        else:
            print("... no i2c found, trying wifi")

            import wifi
            if not wifi.init(self):
                print("... no wifi found.")
                self.config.wifi_attached = False
            else:
                self.config.wifi_attached = True

        self.config.developer_mode = not (usb_cdc.data is None)

    def _init_applications(self):
        app_store_and_show = StoreAndShowApp(self)
        app_fetch = FetchApp(self)
        app_third = FetchApp(self)

        def start_app(app):
            if self.active_app:
                self.active_app.unrun()

            self.active_app = app
            self.active_app.run()

        self.subscribe(MessageKey.BUTTON_A_RELEASED, lambda os, _: start_app(app_store_and_show))
        self.subscribe(MessageKey.BUTTON_B_RELEASED, lambda os, _: start_app(app_fetch))
        self.subscribe(MessageKey.BUTTON_C_RELEASED, lambda os, _: start_app(app_third))

        # register special terminal showing button
        self.subscribe(MessageKey.BUTTON_DEVELOPER_RELEASED,
                       lambda os, _: os.messages.append(
                           Message(ui.MessageKey.SHOW_TERMINAL)
                       ))

        start_app(app_store_and_show)


class SystemButtons:
    def __init__(self):
        self.a = _system_button(board.SW_A)
        self.b = _system_button(board.SW_B)
        self.c = _system_button(board.SW_C)
        self.up = _system_button(board.SW_UP)
        self.down = _system_button(board.SW_DOWN)
        self.developer = _system_button(board.USER_SW)
        self.last = self.snapshot()

    def snapshot(self):
        return {
            'a': self.a.value,
            'b': self.b.value,
            'c': self.c.value,
            'up': self.up.value,
            'down': self.down.value,
            'developer': self.developer.value,
        }

    def changes(self):
        result = {}
        current = self.snapshot()

        if current['a'] != self.last['a']: result['a'] = current['a']
        if current['b'] != self.last['b']: result['b'] = current['b']
        if current['c'] != self.last['c']: result['c'] = current['c']
        if current['up'] != self.last['up']: result['up'] = current['up']
        if current['down'] != self.last['down']: result['down'] = current['down']
        if current['developer'] != self.last['developer']: result['developer'] = current['developer']

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
        os.messages.append(Message(MessageKey.BUTTON_CHANGED, changes))

        for button in changes:
            pressed = changes[button]
            key = _button_to_message_key(button, pressed)
            os.messages.append(Message(key, (button, pressed)))


def _button_to_message_key(button, pressed):
    if pressed:
        state = "PRESSED"
    else:
        state = "RELEASED"
    key = f"SYSTEM_BUTTON_{button}_{state}"

    if key in MessageKey.__members__:
        return MessageKey[key]


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


def _serial_received_handler(os, message):
    (command, meta, payload) = message.value
    if command in SERIAL_COMMANDS:
        SERIAL_COMMANDS[command](os, meta, payload)


def _reload_command(os, meta, payload):
    os.messages.append(Message(MessageKey.RELOAD))


def _exit_command(os, meta, payload):
    os.messages.append(Message(MessageKey.EXIT))


def _terminal_command(os, meta, payload):
    os.messages.append(Message(ui.MessageKey.SHOW_TERMINAL))


def _refresh_command(os, meta, payload):
    os.messages.append(Message(ui.MessageKey.REFRESH))


def _config_save_command(os, meta, payload):
    os.messages.append(Message(MessageKey.CONFIG_SAVE))


def _config_load_command(os, meta, payload):
    os.messages.append(Message(MessageKey.CONFIG_LOAD))


def _config_update_command(os, meta, payload):
    os.messages.append(Message(MessageKey.CONFIG_UPDATE))


def _config_list_command(os, meta, payload):
    os.messages.append(Message(MessageKey.CONFIG_LIST))


def _show_command(os, filename, _):
    os.messages.append(Message(ui.MessageKey.SHOW_FILE, filename))


def _store_command(os, filename, payload):
    if not filename.endswith('.b64'):
        filename += '.b64'

    with open(filename, "wb") as file:
        file.write(payload)


def _preview_command(os, meta, payload):
    bitmap, palette = ui.decode_serialized_bitmap(payload)
    del payload

    os.messages.append(Message(MessageKey.INFO, 'previewing image'))
    os.messages.append(Message(ui.MessageKey.SHOW_BITMAP, (bitmap, palette)))


def _list_command(os, meta, payload):
    files = ",".join(os.get_stored_files())

    os.messages.append(Message(MessageKey.INFO, f"Sending file list: '{files}'."))
    os.messages.append(Message(serial.MessageKey.RESPOND, files))


def _delete_command(os, filename, _):
    if not filename.endswith('.b64'):
        filename += '.b64'

    files = os.get_stored_files()
    if filename in files:
        os.messages.append(Message(MessageKey.INFO, f"Deleted file: '{filename}'."))
        systemos.remove(filename)


def _help_command(os, meta, payload):
    message = ','.join(SERIAL_COMMANDS.keys())
    os.messages.append(Message(MessageKey.INFO, f"Available commands: {message}"))
    os.messages.append(Message(serial.MessageKey.RESPOND, message))


SERIAL_COMMANDS = {
    "help": _help_command,

    "reload": _reload_command,
    "exit": _exit_command,
    "terminal": _terminal_command,
    "refresh": _refresh_command,

    "config_save": _config_save_command,
    "config_load": _config_load_command,
    "config_update": _config_update_command,
    "config_list": _config_list_command,

    "show": _show_command,
    "store": _store_command,
    "preview": _preview_command,
    "list": _list_command,
    "delete": _delete_command,
}
