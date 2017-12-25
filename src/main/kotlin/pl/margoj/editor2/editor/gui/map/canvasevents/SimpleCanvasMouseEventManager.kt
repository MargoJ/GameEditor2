package pl.margoj.editor2.editor.gui.map.canvasevents

import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEventListener
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.Point

class SimpleCanvasMouseEventManager : CanvasMouseEventManager()
{
    override val parentListener: AdvancedMouseEventListener? = null

    override fun getCursor(x: Int, y: Int): Selection? = null

    override fun updateCursorInfo(point: Point?)
    {
    }
}