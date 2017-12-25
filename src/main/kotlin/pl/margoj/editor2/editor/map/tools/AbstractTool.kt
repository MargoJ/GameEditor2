package pl.margoj.editor2.editor.map.tools

import org.apache.commons.lang3.Validate
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvent
import pl.margoj.editor2.editor.map.undo.CollisionsChangeAction
import pl.margoj.editor2.editor.map.undo.TilesChangeAction
import pl.margoj.editor2.editor.map.undo.WaterChangeAction
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.fragment.MapFragment

abstract class AbstractTool(val mapEditor: MapEditor) : Tool
{
    private var lastLocation: Point? = null
    private lateinit var oldFragments: HashMap<Point, MapFragment>
    private lateinit var newFragments: HashMap<Point, MapFragment>
    private lateinit var oldCollisions: HashMap<Point, Boolean>
    private lateinit var newCollisions: HashMap<Point, Boolean>
    private lateinit var oldWater: HashMap<Point, Int>
    private lateinit var newWater: HashMap<Point, Int>

    protected fun reportFragmentChange(old: MapFragment, new: MapFragment)
    {
        Validate.isTrue(old.point == new.point, "Invalid fragments")
        this.oldFragments.putIfAbsent(old.point, old)
        this.newFragments.put(new.point, new)
    }

    protected fun reportCollisionChange(point: Point, old: Boolean, new: Boolean)
    {
        this.oldCollisions.putIfAbsent(point, old)
        this.newCollisions.put(point, new)
    }

    protected fun reportWaterLevelChange(point: Point, old: Int, new: Int)
    {
        this.oldWater.putIfAbsent(point, old)
        this.newWater.put(point, new)
    }

    override final fun mousePressed(event: AdvancedMouseEvent)
    {
        when (this.mapEditor.currentLayer)
        {
            MargoMap.COLLISION_LAYER ->
            {
                this.oldCollisions = HashMap()
                this.newCollisions = HashMap()
            }
            MargoMap.WATER_LAYER ->
            {
                this.oldWater = HashMap()
                this.newWater = HashMap()
            }
            else ->
            {
                this.oldFragments = HashMap()
                this.newFragments = HashMap()
            }
        }
        val currentPoint = this.mapEditor.canvasPointToRendererPoint(event.currentPoint / this.mapEditor.renderer.pointSize)

        this.mousePressed0(event, currentPoint)
    }

    protected abstract fun mousePressed0(event: AdvancedMouseEvent, point: Point)

    override final fun mouseDragged(event: AdvancedMouseEvent)
    {
        val currentPoint = this.mapEditor.canvasPointToRendererPoint(event.currentPoint / this.mapEditor.renderer.pointSize)

        if (currentPoint == lastLocation)
        {
            return
        }

        this.lastLocation = currentPoint

        this.mouseDragged0(event, currentPoint)
    }

    protected abstract fun mouseDragged0(event: AdvancedMouseEvent, point: Point)

    override final fun mouseReleased(event: AdvancedMouseEvent)
    {
        val currentPoint = this.mapEditor.canvasPointToRendererPoint(event.currentPoint / this.mapEditor.renderer.pointSize)

        when (this.mapEditor.currentLayer)
        {
            MargoMap.COLLISION_LAYER ->
            {
                if (this.oldCollisions.isNotEmpty())
                {
                    this.mapEditor.addUndoAction(CollisionsChangeAction(this.mapEditor, this.mapEditor.currentMap!!, this.oldCollisions, this.newCollisions))
                }
            }
            MargoMap.WATER_LAYER ->
            {
                if (this.oldWater.isNotEmpty())
                {
                    this.mapEditor.addUndoAction(WaterChangeAction(this.mapEditor, this.mapEditor.currentMap!!, this.oldWater, this.newWater))
                }
            }
            else ->
            {
                if (this.oldFragments.isNotEmpty())
                {
                    this.mapEditor.addUndoAction(TilesChangeAction(this.mapEditor, this.mapEditor.currentMap!!, this.oldFragments.values, this.newFragments.values))
                }
            }
        }

        this.mouseReleased0(event, currentPoint)
    }

    protected abstract fun mouseReleased0(event: AdvancedMouseEvent, point: Point)
}