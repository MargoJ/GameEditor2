package pl.margoj.editor2.utils

import java.awt.Color
import java.util.*

object GroupColors
{
    private const val BEST_MAGIC_NUMBER_EVER_CREATED = 241468207951829

    private val random = Random(BEST_MAGIC_NUMBER_EVER_CREATED)
    private val colors = ArrayList<Color>()

    init
    {
        colors.add(Color(0.0f, 0.0f, 0.0f, 0.0f))
    }

    fun getColorFor(x: Int): Color
    {
        while (x >= colors.size)
        {
            colors.add(Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0f))
        }

        return colors[x]
    }
}