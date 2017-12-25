package pl.margoj.editor2.editor.gui.map.canvasevents

import javafx.scene.Cursor
import javafx.scene.control.ScrollBar
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvent
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEventListener
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvents
import pl.margoj.editor2.editor.map.render.MapRenderer
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.Point

abstract class CanvasMouseEventManager : AdvancedMouseEventListener
{
    private val canvas
        get() = this.mapRenderer.canvas

    lateinit var mapRenderer: MapRenderer

    private var mouseStartX = 0
    private var mouseStartY = 0
    private var lastMouseX = -1
    private var lastMouseY = -1
    private var previousCursor: Selection? = null

    abstract val parentListener: AdvancedMouseEventListener?

    abstract fun getCursor(x: Int, y: Int): Selection?

    abstract fun updateCursorInfo(point: Point?)

    fun init(mapRenderer: MapRenderer)
    {
        this.mapRenderer = mapRenderer

        canvas.setOnMouseMoved {
            this.updateCursor(it)
        }

        canvas.setOnMouseExited {
            val oldCursor = this.mapRenderer.cursorSelection
            if (oldCursor != null)
            {
                this.mapRenderer.cursorSelection = null
                this.mapRenderer.redrawFragment(oldCursor)
            }
            this.updateCursorInfo(null)
        }

        val events = AdvancedMouseEvents(canvas, this)
        events.register()
    }

    private fun updateCursor(event: MouseEvent)
    {
        val shiftX = this.mapRenderer.shiftX
        val shiftY = this.mapRenderer.shiftY

        val x = event.x.toInt() / this.mapRenderer.pointSize
        val y = event.y.toInt() / this.mapRenderer.pointSize

        if (x != this.lastMouseX || y != this.lastMouseY)
        {
            this.mapRenderer.cursorSelection = null

            if (this.previousCursor != null)
            {
                this.mapRenderer.redrawFragment(this.previousCursor!!)
            }

            val cursorX = shiftX + x
            val cursorY = shiftY + y

            val cursor = this.getCursor(cursorX, cursorY)

            if (cursor != null)
            {
                this.mapRenderer.cursorSelection = cursor
                this.mapRenderer.redrawFragment(cursor)

                this.previousCursor = cursor
            }

            this.lastMouseX = x
            this.lastMouseY = y

            this.updateCursorInfo(Point(cursorX, cursorY))
        }
    }

    override fun mousePressed(event: AdvancedMouseEvent)
    {
        this.mouseStartX = event.currentPoint.x
        this.mouseStartY = event.currentPoint.y

        this.parentListener?.mousePressed(event)
    }

    override fun mouseDragged(event: AdvancedMouseEvent)
    {
        // handle map dragging
        if (event.button == MouseButton.MIDDLE)
        {
            canvas.cursor = Cursor.CLOSED_HAND

            if (this.updateScrollbar(this.mapRenderer.mapScrollHorizontal, ((this.mouseStartX - event.currentPoint.x) / this.mapRenderer.pointSize).toDouble()))
            {
                this.mouseStartX = event.currentPoint.x
            }

            if (this.updateScrollbar(this.mapRenderer.mapScrollVertical, ((this.mouseStartY - event.currentPoint.y) / this.mapRenderer.pointSize).toDouble()))
            {
                this.mouseStartY = event.currentPoint.y
            }

            return
        }

        this.updateCursor(event.event)

        this.parentListener?.mouseDragged(event)
    }

    override fun mouseReleased(event: AdvancedMouseEvent)
    {
        canvas.cursor = Cursor.DEFAULT

        this.parentListener?.mouseReleased(event)
    }

    private fun updateScrollbar(bar: ScrollBar, change: Double): Boolean
    {
        val old = bar.value
        bar.value = Math.max(bar.min, Math.min(bar.max, bar.value + change))
        return bar.value != old
    }
}