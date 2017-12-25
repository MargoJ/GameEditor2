package pl.margoj.editor2.editor.map.tileset

import javafx.scene.input.MouseButton
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvent
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEventListener
import pl.margoj.editor2.geometry.RectangleSelection
import pl.margoj.mrf.map.Point

class TilesetMouseEvents(val tilesetManager: TilesetManager) : AdvancedMouseEventListener
{
    private var lastX = -1
    private var lastY = -1

    var temporarySelection: RectangleSelection? = null
    var selection: RectangleSelection? = null

    override fun mousePressed(event: AdvancedMouseEvent)
    {
        if (event.button == MouseButton.PRIMARY)
        {
            this.mouseDragged(event)
        }
    }

    override fun mouseDragged(event: AdvancedMouseEvent)
    {
        if (event.button == MouseButton.PRIMARY)
        {
            val currentX = event.currentPoint.x / 32
            val currentY = event.currentPoint.y / 32

            if (currentY != this.lastY || currentX != this.lastX)
            {
                if (this.tilesetManager.currentTileset?.auto == true)
                {
                    this.temporarySelection = RectangleSelection(
                            Point(currentX, currentY),
                            Point(currentX, currentY)
                    )
                }
                else
                {
                    this.temporarySelection = RectangleSelection(
                            Point(event.startingPoint.x / 32, event.startingPoint.y / 32),
                            Point(currentX, currentY)
                    )
                }

                this.lastX = currentX
                this.lastY = currentY
                this.tilesetManager.redrawTileset()
            }
        }
    }

    override fun mouseReleased(event: AdvancedMouseEvent)
    {
        if (event.button == MouseButton.PRIMARY)
        {
            this.selection = this.temporarySelection
            this.temporarySelection = null
            this.tilesetManager.redrawTileset()
        }
    }
}