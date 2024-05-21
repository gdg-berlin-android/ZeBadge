import traceback


def exception_to_readable(exception) -> str:
    """format a given exception to a readable error message."""
    message = str(exception)
    trace = traceback.format_exception(exception)
    result = "Reason: "
    result += message if len(message) > 0 else "🤷"
    result += "\n"
    result += "\n  ".join(trace)
    return result
