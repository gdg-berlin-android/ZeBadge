class Message:
    # a message that can be sent over
    def __init__(self, topic: str, value=None):
        self.topic = topic
        self.value = value

    def __str__(self):
        return f"{self.topic}: {self.value}"
