package pl.margoj.editor2.editor.map.objects.tools

import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.objects.ObjectTool
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.mapspawn.MapSpawnObject
import java.awt.Graphics2D

class SpawnPointObjectTool : ObjectTool<MapSpawnObject>
{
    private val image = FXUtils.loadAwtImage("objects/respawn.png")

    override val mapObjectType: Class<MapSpawnObject> = MapSpawnObject::class.java

    override val name: String = "Punkt spawnu"

    override fun edit(editor: MapEditor, map: MargoMap, point: Point, mapObject: MapSpawnObject?)
    {
        if (mapObject == null)
        {
            map.objects.filter { it is MapSpawnObject }.forEach { editor.setObject(it.position, null) }
            editor.setObject(point, MapSpawnObject(point))
        }
    }

    override fun delete(editor: MapEditor, map: MargoMap, mapObject: MapSpawnObject)
    {
        editor.setObject(mapObject.position, null)
    }

    override fun contains(editor: MapEditor, mapObject: MapSpawnObject, point: Point): Boolean
    {
        return mapObject.position == point
    }

    override fun draw(editor: MapEditor, mapObject: MapSpawnObject, g: Graphics2D, point: Point)
    {
        g.drawImage(image, 0, 0, null)
    }
}