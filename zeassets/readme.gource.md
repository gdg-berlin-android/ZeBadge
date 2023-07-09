# Gource: How to visualize your source code

Visualizing code is important, here is how.

## Install

> sudo dnf install gource 
`or platform equivivalent`

## Config

Use the [Configuration](gource.conf).

## Application

Run gource:

> gource --load-config gource.conf --output-ppm-stream gource.ppm

This will open a window with the contributions visualized, don't close it is used for recording the ppm.

Convert ppm into mp4

> ffmpeg -y -r 60 -f image2pipe -vcodec ppm -i gource.ppm -vcodec libopenh264 -preset medium -pix_fmt yuv420p -crf 1 -threads 0 -bf 0 gource.mp4

After blindly following this steps, you will have the same thing as the [resulting output mp4](gource.mp4). 

# Enjoy. ♥
