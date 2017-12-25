package pl.margoj.editor2.editor.map.utils

import pl.margoj.mrf.map.Point
import java.awt.image.BufferedImage

class NpcImage(val image: BufferedImage)
{
    val parts: Map<Point, BufferedImage>

    init
    {
        val parts = HashMap<Point, BufferedImage>()

        val width = image.getWidth(null)
        val height = image.getHeight(null)

        val highestX = Math.ceil((Math.ceil(width.toDouble() / 32.0).toInt() - 1) / 2.0).toInt()
        val lowestX = -highestX

        val lowestY = -Math.ceil(height.toDouble() / 32.0).toInt() + 1
        val highestY = 0

        val wholeWidth = (highestX * 2 + 1) * 32
        val wholeHeight = (-lowestY + 1) * 32

        val xPadding = Math.floor((wholeWidth - width).toDouble() / 2.0).toInt()
        val yPadding =  wholeHeight - height

        val wholeImage = BufferedImage(wholeWidth, wholeHeight, BufferedImage.TYPE_INT_ARGB)
        wholeImage.graphics.drawImage(this.image, xPadding, yPadding, null)

        for (x in lowestX..highestX)
        {
            for (y in lowestY..highestY)
            {
                val realImageX = (x - lowestX) * 32
                val realImageY = (y - lowestY) * 32

                parts.put(Point(x, y), wholeImage.getSubimage(realImageX, realImageY, 32, 32))
            }
        }

        this.parts = parts
    }
}