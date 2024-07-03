import os as systemos
import time
import traceback

import board
import usb_cdc
from digitalio import DigitalInOut
from digitalio import Direction
from digitalio import Pull

import serial
import ui
from app_developer_idle_clicker import DeveloperIdleClickerApp
from app_store_and_show import StoreAndShowApp
from app_zealterego import ZeAlterEgoApp
from app_zepass import ZePassApp
from config import fields_to_str
from config import load_config
from config import save_config
from config import update_config
from message import Message


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
    BUTTON_CHANGED = "button_changed"


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
        self._reset_subscribers()
        self._subscribe_to_system_buttons()

        # add defaults
        self.config = {}
        load_config(self.config)

        self._init_interfaces()

        # applications
        self._app_a = None
        self._app_b = None
        self._app_c = None
        self._init_apps()

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
                           Message(serial.MessageKey.RESPOND, fields_to_str(self.config)))
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
                    task(self)

                print('.', end='')

                current_messages = self.messages.copy()
                current_messages += [Message(MessageKey.TICK, None)]
                self.messages.clear()

                for message in current_messages:
                    if message.topic in self.subscribers:
                        subscriber_ids = self.subscribers[message.topic]
                        for subscriber_id in subscriber_ids:
                            subscriber = subscriber_ids[subscriber_id]
                            subscriber(self, message)

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

        keyboard_attached = False
        wifi_attached = False
        if addrs:
            has_keyboard = 95 in addrs
            if has_keyboard:
                import keyboard
                keyboard.init(self)

            keyboard_attached = has_keyboard
        else:
            print("... no i2c found, trying wifi")

            import wifi
            if not wifi.init(self):
                print("... no wifi found.")
                wifi_attached = False
            else:
                print("... wifi !!!")
                wifi_attached = True

        self.config["keyboard.attached"] = keyboard_attached
        self.config["wifi.attached"] = wifi_attached
        self.config["developer.mode"] = not (usb_cdc.data is None)

    def _init_apps(self):
        self._app_a = StoreAndShowApp(self)
        self._app_b = ZeAlterEgoApp(self)

        if self.config["wifi.attached"]:
            self._app_c = ZePassApp(self)
        elif self.config['keyboard.attached']:
            self._app_c = DeveloperIdleClickerApp(self)
        else:
            self._app_c = DeveloperIdleClickerApp(self)

        self._start_app(self._app_a)

    def _start_app(self, app):
        if self.active_app == app:
            return

        if self.active_app:
            self.messages.append(Message(MessageKey.INFO, f"Stopping app {self.active_app}."))
            self.active_app.unrun()

        self.messages.append(Message(MessageKey.INFO, f"Starting app {app}."))
        self.active_app = app
        self.active_app.run()

    def _check_system_keys(self, changed):
        if 'developer' in changed and not changed['developer']:
            self.messages.append(Message(ui.MessageKey.SHOW_TERMINAL))
        else:
            if 'a' in changed and not changed['a']:
                app = self._app_a
            elif 'b' in changed and not changed['b']:
                app = self._app_b
            elif 'c' in changed and not changed['c']:
                app = self._app_c
            else:
                app = None

            if app:
                self._start_app(app)

    def _subscribe_to_system_buttons(self):
        self.subscribe(MessageKey.BUTTON_CHANGED, lambda os, message: self._check_system_keys(message.value))


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
    os.messages.append(Message(MessageKey.CONFIG_UPDATE, payload))


def _config_list_command(os, meta, payload):
    os.messages.append(Message(MessageKey.CONFIG_LIST))


def _show_command(os, filename, _):
    _save_last_page(filename)
    os.messages.append(Message(ui.MessageKey.SHOW_FILE, filename))


def _store_command(os, filename, payload):
    if not filename.endswith('.b64'):
        filename += '.b64'

    _save_last_page(filename)

    with open(filename, "wb") as file:
        file.write(payload)


def _save_last_page(filename):
    try:
        open('.last_badge', 'w').write(filename)
    except OSError:
        print("OS Error (developer mode?)")


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
