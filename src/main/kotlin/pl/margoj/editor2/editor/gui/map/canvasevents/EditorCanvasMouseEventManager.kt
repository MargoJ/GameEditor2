package pl.margoj.editor2.editor.gui.map.canvasevents

import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEventListener
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.Point

class EditorCanvasMouseEventManager(val editor: MapEditor) : CanvasMouseEventManager()
{
    override val parentListener: AdvancedMouseEventListener?
        get() = this.editor.currentTool

    override fun getCursor(x: Int, y: Int): Selection?
    {
        if (this.editor.currentMap == null)
        {
            return null
        }

        return this.editor.currentTool.cursorShape.absolutize().relativize(Point(x, y))
    }

    override fun updateCursorInfo(point: Point?)
    {
        val label = this.editor.gui.editorGUI.controller.labelCursorInfo

        if (point == null)
        {
            label.text = ""
        }
        else
        {
            val (cursorX, cursorY) = point

            if (this.editor.currentMap != null && cursorX >= 0 && cursorY >= 0 && cursorX < this.editor.currentMap!!.width && cursorY < this.editor.currentMap!!.height)
            {
                label.text = "$cursorX,$cursorY"
            }
            else
            {
                label.text = ""
            }
        }
    }
}