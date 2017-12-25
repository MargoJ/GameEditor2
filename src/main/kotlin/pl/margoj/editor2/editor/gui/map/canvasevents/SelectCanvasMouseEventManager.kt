package pl.margoj.editor2.editor.gui.map.canvasevents

import javafx.scene.input.MouseButton
import javafx.stage.Stage
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvent
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEventListener
import pl.margoj.editor2.editor.map.render.MapRenderer
import pl.margoj.editor2.geometry.RectangleSelection
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.Point

class SelectCanvasMouseEventManager(stage: Stage, renderer: MapRenderer, callback: (Point) -> Unit) : CanvasMouseEventManager()
{
    override val parentListener: AdvancedMouseEventListener = Listener(stage, renderer, callback)

    override fun getCursor(x: Int, y: Int): Selection? = RectangleSelection(x, y, 1, 1)

    override fun updateCursorInfo(point: Point?)
    {
    }

    private class Listener(val stage: Stage, val renderer: MapRenderer, val callback: (Point) -> Unit) : AdvancedMouseEventListener
    {
        override fun mousePressed(event: AdvancedMouseEvent)
        {
        }

        override fun mouseDragged(event: AdvancedMouseEvent)
        {
        }

        override fun mouseReleased(event: AdvancedMouseEvent)
        {
            if (event.button != MouseButton.PRIMARY || event.dragged)
            {
                return
            }

            this.callback((event.currentPoint / this.renderer.pointSize).getRelative(this.renderer.shiftX, this.renderer.shiftY))
            this.stage.close()
        }
    }
}