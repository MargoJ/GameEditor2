package pl.margoj.editor2.editor.map.tools

import javafx.scene.input.MouseButton
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvent
import pl.margoj.editor2.editor.map.undo.SelectionChangeAction
import pl.margoj.editor2.geometry.RectangleSelection
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.Point

class ToolSelect(val mapEditor: MapEditor) : Tool
{
    override val cursorShape: Selection = RectangleSelection(0, 0, 1, 1)

    private lateinit var previousSelection: Selection
    private lateinit var startPoint: Point
    private var lastPoint: Point? = null

    override fun mousePressed(event: AdvancedMouseEvent)
    {
        this.previousSelection = this.mapEditor.selection

        if (event.button == MouseButton.SECONDARY)
        {
            this.mapEditor.selection = RectangleSelection(0, 0, this.mapEditor.currentMap!!.width, this.mapEditor.currentMap!!.height)

            this.mapEditor.renderer.resetCacheAndRedraw(this.previousSelection)
            this.mapEditor.renderer.resetCacheAndRedraw(this.mapEditor.selection)

            return
        }

        if (event.button != MouseButton.PRIMARY)
        {
            return
        }

        this.startPoint = this.mapEditor.canvasPointToRendererPoint(event.currentPoint / this.mapEditor.renderer.pointSize)

        this.mouseDragged(event)
    }

    override fun mouseDragged(event: AdvancedMouseEvent)
    {
        if (event.button != MouseButton.PRIMARY)
        {
            return
        }

        val point = this.mapEditor.canvasPointToRendererPoint(event.currentPoint / this.mapEditor.renderer.pointSize)

        if (point == this.lastPoint)
        {
            return
        }

        this.lastPoint = point

        val previousSelection = this.mapEditor.selection

        // set new selection
        this.mapEditor.selection = RectangleSelection(this.startPoint, point)

        // render to clear previous selection
        this.mapEditor.renderer.resetCacheAndRedraw(previousSelection)

        // render current selection
        this.mapEditor.renderer.resetCacheAndRedraw(this.mapEditor.selection)
    }

    override fun mouseReleased(event: AdvancedMouseEvent)
    {
        if (event.button != MouseButton.PRIMARY && event.button != MouseButton.SECONDARY)
        {
            return
        }

        this.mapEditor.addUndoAction(SelectionChangeAction(this.mapEditor, this.previousSelection, this.mapEditor.selection))
    }
}