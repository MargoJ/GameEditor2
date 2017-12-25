package pl.margoj.editor2.editor.map.tools

import javafx.scene.input.MouseButton
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvent
import pl.margoj.editor2.geometry.RectangleSelection
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point

class FillingTool(mapEditor: MapEditor) : AbstractTool(mapEditor)
{
    override fun mousePressed0(event: AdvancedMouseEvent, point: Point)
    {
        val map = mapEditor.currentMap!!

        if (point !in this.mapEditor.selection)
        {
            return
        }

        when (this.mapEditor.currentLayer)
        {
            MargoMap.WATER_LAYER ->
            {
                var waterLevel = map.getWaterLevelAt(point)

                when
                {
                    event.button == MouseButton.PRIMARY -> waterLevel++
                    event.button == MouseButton.MIDDLE -> waterLevel--
                    else -> return
                }

                waterLevel = Math.max(0, waterLevel)
                waterLevel = Math.min(8, waterLevel)

                for (selectionPoint in this.mapEditor.selection)
                {
                    val oldWater = map.getWaterLevelAt(selectionPoint)

                    if (oldWater != waterLevel)
                    {
                        map.setWaterLevelAt(selectionPoint, waterLevel)
                        this.reportWaterLevelChange(selectionPoint, oldWater, waterLevel)
                    }
                }

                this.mapEditor.renderer.resetCacheAndRedraw(this.mapEditor.selection)
            }
            MargoMap.COLLISION_LAYER ->
            {
                if (event.button != MouseButton.PRIMARY)
                {
                    return
                }

                val collision = !map.getCollisionAt(point)

                for (selectionPoint in this.mapEditor.selection)
                {
                    val oldCollision = map.getCollisionAt(selectionPoint)

                    if (oldCollision != collision)
                    {
                        map.setCollisionAt(selectionPoint, collision)
                        this.reportCollisionChange(selectionPoint, oldCollision, collision)
                    }
                }

                this.mapEditor.renderer.resetCacheAndRedraw(this.mapEditor.selection)
            }
            else ->
            {
                if (event.button != MouseButton.PRIMARY)
                {
                    return
                }

                val tileset = this.mapEditor.tilesetManager.currentTileset ?: return
                val mapSelection = this.mapEditor.selection
                val tilesetSelection = this.mapEditor.tilesetManager.selection as? RectangleSelection ?: return

                for (selectionPoint in mapSelection)
                {
                    val tileXCoordinate = (selectionPoint.x - mapSelection.lowestX) % tilesetSelection.width
                    val tileYCoordinate = (selectionPoint.y - mapSelection.lowestY) % tilesetSelection.height
                    val tilesetPoint = Point(tilesetSelection.x + tileXCoordinate, tilesetSelection.y + tileYCoordinate)

                    val oldFragment = map.getFragment(selectionPoint, this.mapEditor.currentLayer)!!
                    val fragment = tileset.getFragmentAt(map, tilesetPoint, selectionPoint, this.mapEditor.currentLayer)

                    map.setFragment(fragment)
                    this.reportFragmentChange(oldFragment, fragment)
                }

                this.mapEditor.renderer.smartRedraw(this.mapEditor.selection)
            }
        }
    }

    override fun mouseDragged0(event: AdvancedMouseEvent, point: Point)
    {

    }

    override fun mouseReleased0(event: AdvancedMouseEvent, point: Point)
    {

    }

    override val cursorShape: Selection = RectangleSelection(0, 0, 1, 1)

}