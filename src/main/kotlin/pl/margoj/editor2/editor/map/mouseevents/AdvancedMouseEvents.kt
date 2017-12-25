package pl.margoj.editor2.editor.map.mouseevents

import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import pl.margoj.mrf.map.Point

class AdvancedMouseEvents(val node: Canvas, val listener: AdvancedMouseEventListener)
{
    private var currentStartX = 0
    private var currentStartY = 0
    private var dragged = false

    fun register()
    {
        node.setOnMousePressed {
            this.currentStartX = it.x.toInt()
            this.currentStartY = it.y.toInt()
            this.dragged = false

            this.listener.mousePressed(this.createEvent(it))
        }

        node.setOnMouseDragged {
            if(it.x < 0 || it.y < 0 || it.x >= this.node.width || it.y >= this.node.height)
            {
                return@setOnMouseDragged
            }

            this.dragged = true
            this.listener.mouseDragged(this.createEvent(it))
        }

        node.setOnMouseReleased {
            this.listener.mouseReleased(this.createEvent(it))
        }
    }

    private fun createEvent(it: MouseEvent): AdvancedMouseEvent
    {
        return AdvancedMouseEvent(it, it.button, Point(this.currentStartX, this.currentStartY), Point(it.x.toInt(), it.y.toInt()), this.dragged)
    }
}