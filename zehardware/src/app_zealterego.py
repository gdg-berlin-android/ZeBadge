import displayio

import board
import zeos
from message import Message
from ui import MessageKey as UIKeys


class ZeAlterEgoApp:
    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os
        self.subscription_ids = []

    def run(self):
        self.subscription_ids += [
            self.os.subscribe(
                zeos.MessageKey.BUTTON_CHANGED,
                lambda os, message: self._show()
            ),
        ]

        self._show()

    def unrun(self):
        for subscription in self.subscription_ids:
            self.os.unsubscribe(subscription)

    def _show(self):
        swift = board.DISPLAY.is_zebadge()
        print(swift)
        self.os.messages.append(
            Message(
                UIKeys.SHOW_TEXT,
                swift
            )
        )
