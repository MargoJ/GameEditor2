package pl.margoj.editor2.editor.map.objects

import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.MapObject
import java.awt.Graphics2D

interface ObjectTool<T : MapObject<*>>
{
    val mapObjectType: Class<T>

    val name: String

    fun edit(editor: MapEditor, map: MargoMap, point: Point, mapObject: T?)

    fun delete(editor: MapEditor, map: MargoMap, mapObject: T)

    fun draw(editor: MapEditor, mapObject: T, g: Graphics2D, point: Point)

    fun contains(editor: MapEditor, mapObject: T, point: Point): Boolean

    fun getPointsContaining(editor: MapEditor, mapObject: T, map: MargoMap): ArrayList<Point>
    {
        val points = ArrayList<Point>(map.width * map.height)

        for (x in 0 until map.width)
        {
            for (y in 0 until map.height)
            {
                val point = Point(x, y)

                if (this.contains(editor, mapObject, point))
                {
                    points.add(point)
                }
            }
        }

        return points
    }
}