package pl.margoj.editor2.editor.map.tools

import javafx.scene.input.MouseButton
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvent
import pl.margoj.editor2.geometry.RectangleSelection
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import java.util.Collections
import kotlin.collections.HashSet

class SingleElementTool(mapEditor: MapEditor) : AbstractTool(mapEditor)
{
    private var collisionSet = false
    private var currentWaterLevel = 0

    override val cursorShape: Selection
        get()
        {
            val selection = mapEditor.tilesetManager.selection

            if (selection == null || mapEditor.currentLayer == MargoMap.COLLISION_LAYER || mapEditor.currentLayer == MargoMap.WATER_LAYER)
            {
                return RectangleSelection(0, 0, 1, 1)
            }
            else
            {
                return selection
            }
        }

    override fun mousePressed0(event: AdvancedMouseEvent, point: Point)
    {
        if (mapEditor.currentLayer == MargoMap.COLLISION_LAYER)
        {
            this.collisionSet = this.mapEditor.currentMap?.getCollisionAt(point)?.not() ?: return
        }

        if (mapEditor.currentLayer == MargoMap.WATER_LAYER)
        {
            this.currentWaterLevel = this.mapEditor.currentMap!!.getWaterLevelAt(point)

            if (event.button == MouseButton.MIDDLE)
            {
                this.currentWaterLevel--
            }
            else
            {
                this.currentWaterLevel++
            }

            this.currentWaterLevel = Math.max(0, this.currentWaterLevel)
            this.currentWaterLevel = Math.min(8, this.currentWaterLevel)
        }

        this.set(event, point)
    }

    override fun mouseDragged0(event: AdvancedMouseEvent, point: Point)
    {
        this.set(event, point)
    }

    override fun mouseReleased0(event: AdvancedMouseEvent, point: Point)
    {
    }

    private fun set(event: AdvancedMouseEvent, currentPoint: Point)
    {
        val map = this.mapEditor.currentMap ?: return

        when (this.mapEditor.currentLayer)
        {
            MargoMap.WATER_LAYER ->
            {
                if (event.button != MouseButton.PRIMARY && event.button != MouseButton.MIDDLE)
                {
                    return
                }

                if (currentPoint !in this.mapEditor.selection)
                {
                    return
                }

                this.reportWaterLevelChange(currentPoint, map.getWaterLevelAt(currentPoint), this.currentWaterLevel)

                map.setWaterLevelAt(currentPoint, this.currentWaterLevel)
                this.mapEditor.renderer.resetCacheAndRedraw(Selection(Collections.singletonList(currentPoint)))
            }
            MargoMap.COLLISION_LAYER ->
            {
                if (event.button != MouseButton.PRIMARY)
                {
                    return
                }

                if (currentPoint !in this.mapEditor.selection)
                {
                    return
                }

                this.reportCollisionChange(currentPoint, map.getCollisionAt(currentPoint), this.collisionSet)

                map.setCollisionAt(currentPoint, this.collisionSet)
                this.mapEditor.renderer.resetCacheAndRedraw(Selection(Collections.singletonList(currentPoint)))
            }
            else ->
            {
                if (event.button != MouseButton.PRIMARY)
                {
                    return
                }

                val tilesetSelection = this.mapEditor.tilesetManager.selection ?: return
                val tileset = this.mapEditor.tilesetManager.currentTileset ?: return
                val changes = HashSet<Point>()

                for (point in tilesetSelection.points)
                {
                    val cursorPoint = currentPoint.getRelative(point.x - tilesetSelection.lowestX, point.y - tilesetSelection.lowestY)

                    if (cursorPoint !in this.mapEditor.selection)
                    {
                        continue
                    }

                    val oldFragment = map.getFragment(cursorPoint, this.mapEditor.currentLayer)!!

                    val fragment = tileset.getFragmentAt(map, point, cursorPoint, this.mapEditor.currentLayer)
                    map.setFragment(fragment)

                    this.reportFragmentChange(oldFragment, fragment)

                    changes.add(cursorPoint)
                }

                this.mapEditor.renderer.smartRedraw(Selection(changes))
            }
        }
    }
}