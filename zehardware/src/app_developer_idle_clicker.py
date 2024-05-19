import gc
import math
import zeos
import ui
from message import Message
import displayio
import vectorio
import terminalio
from adafruit_display_text import label


class Item:
    """An item that can be purchased for lines of code. Don't ask."""

    def __init__(self, name: str, costs: list[int], output: int, index: int):
        self.name = name
        self.costs = costs
        self.output = output
        self.sprite_index = index


_PROGRAMMER = ""

_ITEMS_ = [
    Item("programmer", [10, 100, 1000, 10000], 1, 0),
    Item("better ide", [20, 30, 40, 50], 2, 1),
    Item("better hardware", [100, 200, 250, 300, 350], 2, 2),
    Item("blue filter glasses", [80, 80, 80, 80], 2, 3),
    Item("bella", [80], 2, 4),
    Item("ziggy", [80], 2, 5),
    Item("third pair of hands", [160], 2, 6),
    Item("switch one class from java to kotlin", [320], 2, 7),
]

_SYMBOL_PIXEL_SIZE = 8
_SCREEN_WIDTH = 296
_SCREEN_HEIGHT = 128
_HORIZONTAL_SYMBOL_COUNT = 17
_VERTICAL_SYMBOL_COUNT = 7

_POSITION_SYMBOLS = [
    # indexes for a rectangular spiral of coordinates. a zero here means,
    # that the index represents the location on screen.
    # @formatter:off
    #                                          1   1   1   1   1   1    1,
    #0     1   2   3   4   5   6   7   8   9   0   1   2   3   4   5    6,
    112,  98, 90, 76, 62, 46, 47, 48, 25, 26, 27, 28, 49, 63, 77, 91, 105,
    113,  99, 89, 75, 61, 45, 23, 24,  9, 10, 11, 29, 50, 64, 78, 92, 106,
    114, 100, 88, 74, 60, 44, 22,  8,  1,  2, 12, 30, 51, 65, 79, 93, 107,
    115, 101, 87, 73, 59, 43, 21,  7,  0,  3, 13, 31, 52, 66, 80, 94, 108,
    116, 102, 86, 72, 58, 42, 20,  6,  5,  4, 14, 32, 53, 67, 81, 95, 109,
    117, 103, 85, 71, 57, 41, 19, 18, 17, 16, 15, 33, 54, 68, 82, 96, 110,
    118, 104, 84, 70, 56, 40, 39, 38, 37, 36, 35, 34, 55, 69, 83, 97, 111,
    # @formatter:on
]

_SYMBOL_OFFSET_X = (_SCREEN_WIDTH - _HORIZONTAL_SYMBOL_COUNT * _SYMBOL_PIXEL_SIZE) / 2
_SYMBOL_OFFSET_Y = (_SCREEN_HEIGHT - _VERTICAL_SYMBOL_COUNT * _SYMBOL_PIXEL_SIZE) / 2

_SYMBOL_POSITIONS: list[tuple[float, float]] = len(_POSITION_SYMBOLS) * [(0.0, 0.0)]


class DeveloperIdleClickerApp:
    """This is an idle clicker for developers: Every "second" a new line of code is created. Buy more developers,
    better tools and things with the lines of code."""

    def __init__(self, os: zeos.ZeBadgeOs):
        global _POSITION_SYMBOLS
        global _SYMBOL_OFFSET_X
        global _SYMBOL_OFFSET_Y
        global _SYMBOL_POSITIONS

        self.os = os

        self.lines_of_code = 0
        self.needs_refresh = True

        # TODO: REMOVE ME ONCE VISUALIZATION IS FINE
        self.inventory: list[Item] = [_ITEMS_[0]]

        self._subscription_ids = []
        for i, v in enumerate(_POSITION_SYMBOLS):
            x = _SYMBOL_OFFSET_X + int(i % _HORIZONTAL_SYMBOL_COUNT) * _SYMBOL_PIXEL_SIZE
            y = _SYMBOL_OFFSET_Y + int(i / _HORIZONTAL_SYMBOL_COUNT) * _SYMBOL_PIXEL_SIZE
            _SYMBOL_POSITIONS[v] = (int(x), int(y))

        del _POSITION_SYMBOLS

        self.atlas = displayio.OnDiskBitmap("./idle_resources.bmp")

    def run(self):
        self._subscription_ids += [
            self.os.subscribe(
                zeos.MessageKey.BUTTON_CHANGED,
                lambda os, message: self._buttons_changed(message.value)
            ),
            self.os.subscribe(
                zeos.MessageKey.TICK,
                lambda os, message: self._ticked()
            ),
        ]

    def unrun(self):
        for subscription_id in self._subscription_ids:
            self.os.unsubscribe(subscription_id)

    def _buttons_changed(self, changed_keys):
        if 'up' in changed_keys and not changed_keys['up']:
            self._up_released()
        if 'down' in changed_keys and not changed_keys['down']:
            self._down_released()

    def _up_released(self):
        self.needs_refresh = True

    def _down_released(self):
        self.needs_refresh = True

    def _refresh(self):
        group = displayio.Group()
        font = terminalio.FONT

        # think about more fancy graphics, a background a thing and a stuff

        for index, item in enumerate(self.inventory):
            sim_x, sim_y = _SYMBOL_POSITIONS[index]

            symbol = displayio.TileGrid(
                bitmap=self.atlas,
                pixel_shader=self.atlas.pixel_shader,
                width=1,
                height=1,
                tile_width=_SYMBOL_PIXEL_SIZE,
                tile_height=_SYMBOL_PIXEL_SIZE,
                default_tile=item.sprite_index,
                x=sim_x,
                y=sim_y,
            )
            group.append(symbol)

            # symbol = item.symbol
            # symbol = f"{index:03d}"
            # text_area = label.Label(
            #     font,
            #     text=symbol,
            #     color=0xFFFFFF,
            # )
            #
            #
            # group.append(text_area)

        score_area = label.Label(
            font,
            scale=2,
            text=f"{self.lines_of_code} loc",
            anchor_point=(0.5, 0.5),
            background_color=0x000000,
            color=0xFFFFFF,
        )
        score_area.x = int(296 / 2)
        score_area.y = 113
        group.append(score_area)

        self.os.messages.append(Message(ui.MessageKey.SHOW_GROUP, group))
        self.needs_refresh = False

    def _calculate_output(self) -> int:
        output = 0
        for item in self.inventory:
            output += item.output

        return output

    def _ticked(self):
        output = self._calculate_output()

        self.lines_of_code += output

        if self.needs_refresh:
            gc.collect()
            self._refresh()
