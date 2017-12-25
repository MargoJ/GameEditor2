package pl.margoj.editor2.editor.map.tileset

import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.paint.Color
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvents
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.tileset.Tileset

class TilesetManager(val mapEditor: MapEditor)
{
    private val tilesetCanvas: Canvas get() = this.mapEditor.gui.editorGUI.controller.tilesetCanvas
    private var tilesetFxImage: Image? = null
    private val events = TilesetMouseEvents(this)

    var currentTileset: Tileset? = null
        set(value)
        {
            field = value

            if (field != null)
            {
                this.tilesetFxImage = SwingFXUtils.toFXImage(field!!.image, null)
            }

            this.events.selection = null
            this.events.temporarySelection = null

            this.redrawTileset()
        }

    val selection: Selection?
        get() = this.events.selection

    fun init()
    {
        AdvancedMouseEvents(this.tilesetCanvas, this.events).register()
    }

    fun redrawTileset()
    {
        val canvas = this.tilesetCanvas
        val g = canvas.graphicsContext2D

        if (this.currentTileset == null)
        {
            canvas.width = 0.0
            canvas.height = 0.0
        }
        else
        {
            val tileset = this.currentTileset!!
            val width = tileset.image.width.toDouble()
            val height = tileset.image.height.toDouble()

            canvas.width = width
            canvas.height = height

            g.fill = Color.BLACK
            g.fillRect(0.0, 0.0, width, height)
            g.drawImage(this.tilesetFxImage, 0.0, 0.0)
        }

        if (events.temporarySelection != null)
        {
            g.stroke = Color.YELLOW
            events.temporarySelection!!.draw(g)
        }

        if (events.selection != null)
        {
            g.stroke = Color.RED
            events.selection!!.draw(g)
        }
    }
}