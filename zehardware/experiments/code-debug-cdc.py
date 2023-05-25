# simple cdc dumper
import usb_cdc

if usb_cdc.data:
    import board
    import terminalio
    import digitalio
    import displayio
    import time
    from adafruit_display_text import label
    
    usb_cdc.data.timeout = 0.5
    
    act = digitalio.DigitalInOut(board.USER_LED)
    act.direction = digitalio.Direction.OUTPUT
    act.value = True
   
    text=f"conncected: <empty>."
    text_area = label.Label(terminalio.FONT, text=text)
    text_area.x = 10
    text_area.y = 10
    board.DISPLAY.show(text_area)
    
    try:
        board.DISPLAY.refresh()
    except Exception as e:
        print("nope: {e}")
    
    iteration = 0
    while True:
        received = usb_cdc.data.readline().decode()
        if len(received) > 0:
            print(f"last received: '{received[:23]}'")
            text=f"received \"{received[:23]}\" ({len(received)})."
            text_area = label.Label(terminalio.FONT, text=text)
            text_area.x = 10
            text_area.y = 10
            board.DISPLAY.show(text_area)
        
            try:
                board.DISPLAY.refresh()
            except Exception as e:
                print(f"nope: {e}")
        
        iteration += 1
        act.value = not act.value

else:
    print("Not Connected to cdc")






