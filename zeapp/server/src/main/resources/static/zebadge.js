async function do_port(port, data) {
    const delay = 600
    await port.open({
        baudRate: 9600,
        dataBits: 8,
        stopBits: 1,
        bufferSize: data.byteLength*3,
        parity: "none"
    })
    sleepFor(delay)

    console.log("post open")
    console.log(port.getInfo())

    const writer = port.writable.getWriter()
    sleepFor(delay)

    writer.write(data)

    sleepFor(delay)
    await writer.releaseLock()
    sleepFor(delay)
    await port.close()
    sleepFor(delay)

    console.log("Done")
}

function sleepFor(sleepDuration){
    var now = new Date().getTime()
    while(new Date().getTime() < now + sleepDuration){
        /* Do nothing */
    }
}

async function on_preview_badge() {
    const canvas = document.getElementById("canvas")
    const image = canvas.toDataURL().split(",")[1]

    const response = await fetch("/api/image/png", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        image: image,
        width: 296,
        height: 128,
        operations: [
          {
            type: "FloydSteinberg",
            width: 296,
            height: 128
          }
        ]
      })
    })

    const blob = await response.text()
    document.getElementById("preview").src = `data:image/png;base64,${blob}`
}

async function on_send_to_badge() {
    if (!navigator.serial) {
        console.log("Serial not supported, enjoy the preview.")
    } else {
        const canvas = document.getElementById("canvas")
        const image = canvas.toDataURL().split(",")[1]

        const response = await fetch("/api/image/bin", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            image: image,
            width: 296,
            height: 128,
            operations: [
              {
                type: "FloydSteinberg",
                width: 296,
                height: 128
              }
            ]
          })
        })

        if (response.ok) {
            const data = await response.text()

            const usbVendorId = 11914
            const port = await navigator.serial.requestPort(
                {filters: [{usbVendorId:usbVendorId}]}
            )

            console.log(data)

            const encoder = new TextEncoder()
            do_port(port, encoder.encode(data))
        } else {
            console.log(`Error: {response}.`)
        }
    }
}

function draw_badge(name, name_size, contact, contact_size) {
    var canvas = document.getElementById("canvas")
    var ctx = canvas.getContext("2d")
    ctx.textAlign = "center"
    ctx.textBaseline = 'middle';

    ctx.fillStyle = "#FFFFFF"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    ctx.fillStyle = "#FF0000"
    ctx.fillRect(0, 0, canvas.width, 30)
    ctx.fillRect(0, canvas.height - 30 , canvas.width, canvas.height)

    ctx.fillStyle = "#FFFFFF"
    ctx.font = `bold 22px Arial`
    ctx.fillText("My name is", canvas.width / 2, 30 / 2)

    ctx.font = `${contact_size}px Arial`
    ctx.fillText(contact, canvas.width / 2, canvas.height - 30 / 2)

    ctx.fillStyle = "#000000"
    ctx.font = `${name_size}px Arial`
    ctx.fillText(name, canvas.width / 2, canvas.height/2)
}

function badge_updated() {
    draw_badge(
        document.getElementById("name").value,
        document.getElementById("name_size").value,
        document.getElementById("contact").value,
        document.getElementById("contact_size").value,
    )
}

