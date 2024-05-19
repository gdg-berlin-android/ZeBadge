import zeos


class TemplateApp:
    """Template app skeleton: se it for continued application experiences."""

    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os

        self._subscription_ids = []

    def run(self):
        self._subscription_ids += [
            self.os.subscribe(
                zeos.MessageKey.BUTTON_CHANGED,
                lambda os, message: self._buttons_changed(message.value)
            ),
        ]

    def unrun(self):
        for subscription_id in self._subscription_ids:
            self.os.unsubscribe(subscription_id)

    def _buttons_changed(self, changed_keys):
        """Do things when up/down buttons were pressed on ZeBadge."""

        if 'up' in changed_keys and not changed_keys['up']:
            print('UP released')
        if 'down' in changed_keys and not changed_keys['down']:
            print('DOWN released')
