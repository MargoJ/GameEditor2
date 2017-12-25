package pl.margoj.editor2.editor.map.objects.tools

import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.objects.ObjectTool
import pl.margoj.editor2.utils.GroupColors
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.npc.NpcMapObject
import java.awt.Graphics2D

class NpcObjectTool : ObjectTool<NpcMapObject>
{
    override val mapObjectType: Class<NpcMapObject> = NpcMapObject::class.java

    override val name: String = "NPC"

    override fun edit(editor: MapEditor, map: MargoMap, point: Point, mapObject: NpcMapObject?)
    {
        editor.gui.openToolGui("npc", map, point, mapObject)
    }

    override fun delete(editor: MapEditor, map: MargoMap, mapObject: NpcMapObject)
    {
        editor.setObject(mapObject.position, null)
    }

    override fun contains(editor: MapEditor, mapObject: NpcMapObject, point: Point): Boolean
    {
        val image = editor.npcCache[mapObject.id] ?: return point == mapObject.position

        return image.parts.keys.contains(point.getRelative(-mapObject.position.x, -mapObject.position.y))
    }

    override fun draw(editor: MapEditor, mapObject: NpcMapObject, g: Graphics2D, point: Point)
    {
        val image = editor.npcCache[mapObject.id] ?: return

        g.drawImage(image.parts[point.getRelative(-mapObject.position.x, -mapObject.position.y)], 0, 0, null)

        if (mapObject.group.toInt() != 0)
        {
            g.color = GroupColors.getColorFor(mapObject.group.toInt())
            g.fillRect(0, 25, 32, 7)
        }
    }
}