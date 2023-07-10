#!python

import sys
from PIL import Image # pip install pillow

if len(sys.argv) < 5 or sys.argv[1] != "-i" or sys.argv[3] != "-o":
    print("Usage:\n\tpython debinarize.py -i <input_file.bin> -o <output_file.bmp>")
    sys.exit(1)

input_file = sys.argv[2]
output_file = sys.argv[4]

with open(input_file, "rb") as file:
    pixels = file.read()

# Let's pretend that we understand this
image_width = 296
image_height = 128
image_size = (image_width, image_height)

pixel_data = [255 if ((pixel >> i) & 1) else 0 for pixel in pixels for i in range(7, -1, -1)]

image = Image.new("L", image_size)
image.putdata(pixel_data)
image.save(output_file)

print("Done.")
