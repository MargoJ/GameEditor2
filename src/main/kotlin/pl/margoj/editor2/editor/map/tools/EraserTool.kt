package pl.margoj.editor2.editor.map.tools

import javafx.scene.input.MouseButton
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvent
import pl.margoj.editor2.geometry.RectangleSelection
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.fragment.empty.EmptyMapFragment
import java.util.*

class EraserTool(mapEditor: MapEditor) : AbstractTool(mapEditor)
{
    override val cursorShape: Selection = RectangleSelection(0, 0, 1, 1)

    override fun mousePressed0(event: AdvancedMouseEvent, point: Point)
    {
        this.erase(event, point)
    }

    override fun mouseDragged0(event: AdvancedMouseEvent, point: Point)
    {
        this.erase(event, point)
    }

    override fun mouseReleased0(event: AdvancedMouseEvent, point: Point)
    {
    }

    fun erase(event: AdvancedMouseEvent, point: Point)
    {
        if (event.button != MouseButton.PRIMARY || point !in this.mapEditor.selection)
        {
            return
        }

        val map = this.mapEditor.currentMap!!

        when (this.mapEditor.currentLayer)
        {
            MargoMap.COLLISION_LAYER ->
            {
                if (map.getCollisionAt(point))
                {
                    map.setCollisionAt(point, false)
                    this.reportCollisionChange(point, true, false)
                    this.mapEditor.renderer.resetCacheAndRedraw(Selection(Collections.singletonList(point)))
                }
            }
            MargoMap.WATER_LAYER ->
            {
                val waterLevel = map.getWaterLevelAt(point)
                if (waterLevel != 0)
                {
                    map.setWaterLevelAt(point, 0)
                    this.reportWaterLevelChange(point, waterLevel, 0)
                    this.mapEditor.renderer.resetCacheAndRedraw(Selection(Collections.singletonList(point)))
                }
            }
            else ->
            {
                val fragment = map.getFragment(point, mapEditor.currentLayer)

                if (fragment != null && fragment !is EmptyMapFragment)
                {
                    val newFragment = EmptyMapFragment(point, mapEditor.currentLayer)
                    map.setFragment(newFragment)
                    this.reportFragmentChange(fragment, newFragment)
                    this.mapEditor.renderer.smartRedraw(Selection(Collections.singletonList(point)))
                }
            }
        }
    }
}