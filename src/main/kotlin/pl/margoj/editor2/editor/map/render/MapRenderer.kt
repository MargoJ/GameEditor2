package pl.margoj.editor2.editor.map.render

import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.ScrollBar
import javafx.scene.image.WritableImage
import javafx.scene.layout.AnchorPane
import org.apache.logging.log4j.LogManager
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.map.objects.ObjectTool
import pl.margoj.editor2.editor.map.objects.ObjectTools
import pl.margoj.editor2.geometry.RectangleSelection
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.MapObject
import pl.margoj.utils.javafx.utils.ResizeableCanvas
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage

abstract class MapRenderer(val editor: MargoJEditor)
{
    private val logger = LogManager.getLogger(MapRenderer::class.java)
    private lateinit var canvasHolder: AnchorPane

    private var currentCanvasWidth = 0
    private var currentCanvasHeight = 0
    private var cache: Array<Array<WritableImage?>>? = null

    var shiftX = 0
        private set
    var shiftY = 0
        private set
    lateinit var canvas: Canvas
        private set
    lateinit var mapScrollHorizontal: ScrollBar
        private set
    lateinit var mapScrollVertical: ScrollBar
        private set

    abstract val pointSize: Int

    abstract var cursorSelection: Selection?

    abstract val selection: Selection?

    abstract val currentMap: MargoMap?

    abstract val drawOnlyCurrentLayer: Boolean

    abstract val currentLayer: Int

    abstract val shouldDrawCollisions: Boolean

    abstract val shouldDrawWater: Boolean

    abstract val shouldDrawGrid: Boolean

    fun init(canvasHolder: AnchorPane, mapScrollHorizontal: ScrollBar, mapScrollVertical: ScrollBar, buttonScrollZero: Button)
    {
        this.mapScrollHorizontal = mapScrollHorizontal
        this.mapScrollVertical = mapScrollVertical
        this.canvasHolder = canvasHolder
        this.canvas = ResizeableCanvas()
        this.canvasHolder.children.setAll(this.canvas)
        AnchorPane.setTopAnchor(this.canvas, 0.0)
        AnchorPane.setLeftAnchor(this.canvas, 0.0)
        AnchorPane.setRightAnchor(this.canvas, 0.0)
        AnchorPane.setBottomAnchor(this.canvas, 0.0)

        this.canvasHolder.heightProperty().addListener { _, _, _ -> this.resizeIfNeeded() }
        this.canvasHolder.widthProperty().addListener { _, _, _ -> this.resizeIfNeeded() }

        mapScrollHorizontal.valueProperty().addListener { _, _, newValue ->
            val old = Math.round(newValue.toDouble()).toInt()
            if (old == this.shiftX)
            {
                return@addListener
            }
            this.shiftX = old

            this.redrawAll()
        }

        mapScrollVertical.valueProperty().addListener { _, _, newValue ->
            val old = Math.round(newValue.toDouble()).toInt()
            if (old == this.shiftY)
            {
                return@addListener
            }
            this.shiftY = old
            this.redrawAll()
        }

        buttonScrollZero.setOnAction {
            mapScrollHorizontal.value = 0.0
            mapScrollVertical.value = 0.0
        }

        this.canvas.setOnScroll {
            this.updateScrollbar(this.mapScrollHorizontal, -it.deltaX / it.multiplierX)
            this.updateScrollbar(this.mapScrollVertical, -it.deltaY / it.multiplierY)
        }
    }

    private fun updateScrollbar(bar: ScrollBar, change: Double): Boolean
    {
        val old = bar.value
        bar.value = Math.max(bar.min, Math.min(bar.max, bar.value + change))
        return bar.value != old
    }

    fun resizeIfNeeded()
    {
        if (this.currentMap == null)
        {
            this.shiftX = 0
            this.shiftY = 0
            this.currentCanvasWidth = (this.canvasHolder.width / this.pointSize).toInt()
            this.currentCanvasHeight = (this.canvasHolder.height / this.pointSize).toInt()
            this.canvas.width = this.currentCanvasWidth * this.pointSize.toDouble()
            this.canvas.height = this.currentCanvasHeight * this.pointSize.toDouble()
            this.canvas.graphicsContext2D.clearRect(0.0, 0.0, this.canvas.width, this.canvas.height)
            return
        }

        this.currentCanvasWidth = (this.canvasHolder.width / this.pointSize).toInt() + 1
        this.currentCanvasHeight = (this.canvasHolder.height / this.pointSize).toInt() + 1

        this.canvas.width = this.currentCanvasWidth * this.pointSize.toDouble()
        this.canvas.height = this.currentCanvasHeight * this.pointSize.toDouble()

        this.recalculateScrollbar(this.mapScrollHorizontal, this.currentCanvasWidth, this.currentMap?.width)
        this.recalculateScrollbar(this.mapScrollVertical, this.currentCanvasHeight, this.currentMap?.height)

        canvas.graphicsContext2D.fill = javafx.scene.paint.Color.BLACK
        canvas.graphicsContext2D.clearRect(0.0, 0.0, this.canvas.width, this.canvas.height)

        this.redrawAll()
    }

    private fun recalculateScrollbar(scrollBar: ScrollBar, size: Int, map: Int?)
    {
        scrollBar.blockIncrement = 1.0
        scrollBar.unitIncrement = 1.0
        scrollBar.visibleAmount = 1.0
        scrollBar.min = 0.0

        scrollBar.valueProperty().addListener { _, _, newValue ->
            if (newValue.toDouble() % 1.0 != 0.0)
            {
                scrollBar.value = Math.round(newValue.toDouble()).toDouble()
            }
        }

        if (map == null || size >= map)
        {
            scrollBar.max = 0.0
            scrollBar.value = 0.0
            scrollBar.isDisable = true
            return
        }

        scrollBar.isDisable = false
        scrollBar.max = (map - size).toDouble() + (if (size % this.pointSize != 0) 1.0 else 0.0)

        scrollBar.value = Math.min(scrollBar.value, scrollBar.max)
        scrollBar.value = Math.max(scrollBar.value, scrollBar.min)
    }

    fun redrawAll()
    {
        if ("redrawAll" in this.editor.debuggingProfile)
        {
            this.logger.trace("redrawAll()")
        }

        val map = this.currentMap
        this.redrawFragment(RectangleSelection(0, 0, map?.width ?: this.currentCanvasWidth, map?.height ?: this.currentCanvasHeight))
    }

    fun redrawFragment(selection: Selection)
    {
        if ("redrawFragment" in this.editor.debuggingProfile)
        {
            this.logger.trace("redrawFragment($selection)")
        }

        var drawCursor = false
        var drawSelection = false

        for (point in selection.points)
        {
            if (!this.inSight(point))
            {
                continue
            }

            this.drawFrame(point)

            if (!drawCursor && this.cursorSelection != null && this.cursorSelection!!.points.contains(point))
            {
                drawCursor = true
            }

            if (!drawSelection && this.selection != null && this.selection!!.contains(point))
            {
                drawSelection = true
            }
        }

        val g = this.canvas.graphicsContext2D

        if (drawCursor)
        {
            g.stroke = javafx.scene.paint.Color.RED
            this.cursorSelection!!.relativize(Point(-this.shiftX, -this.shiftY)).draw(g, this.pointSize)
        }

        if (drawSelection)
        {
            g.stroke = javafx.scene.paint.Color.WHITE
            this.selection!!.relativize(Point(-this.shiftX, -this.shiftY)).draw(g, this.pointSize)
        }

        if (drawSelection || drawCursor)
        {
            // clear borders
            val map = this.currentMap
            if (map !== null)
            {
                val canvasWidthAlignment = (Math.min(this.canvasHolder.width.toInt() / this.pointSize, map.width) * this.pointSize).toDouble()
                val canvasHeightAlignment = (Math.min(this.canvasHolder.height.toInt() / this.pointSize, map.height) * this.pointSize).toDouble()

                if (this.mapScrollHorizontal.value == this.mapScrollHorizontal.max)
                {
                    g.clearRect(canvasWidthAlignment, 0.0, this.canvas.width - canvasWidthAlignment, this.canvas.height)
                }
                if (this.mapScrollVertical.value == this.mapScrollVertical.max)
                {
                    g.clearRect(0.0, canvasHeightAlignment, this.canvas.width, this.canvas.height - canvasHeightAlignment)
                }
            }
        }
    }

    fun inSight(point: Point): Boolean
    {
        return point.x >= this.shiftX && point.y >= this.shiftY && point.x < this.currentCanvasWidth + this.shiftX && point.y < this.currentCanvasHeight + this.shiftY
    }

    private companion object DrawingConstants
    {
        val BACKGROUND_COLOR = Color.BLACK!!
        val GRID_COLOR = Color.YELLOW!!
        val COLLISION_BORDER = Color.YELLOW!!
        val COLLISION_FILL = Color(255, 255, 0, 100)
        val WATER_BORDER = Color(173, 216, 230, 255)
        val WATER_FILL = Color(173, 216, 230, 100)
        val WATER_TEXT_COLOR = Color.BLACK!!
        val WATER_FONT = Font("Default", Font.PLAIN, 18)
    }

    private fun drawFrame(point: Point)
    {
        if ("drawFrame" in this.editor.debuggingProfile)
        {
            this.logger.trace("drawFrame($point)")
        }

        val canvasG = this.canvas.graphicsContext2D
        val map = this.currentMap

        if (map == null || !map.inBounds(point))
        {
            return
        }

        if (this.cache == null)
        {
            this.cache = Array(map.width, { Array(map.height, { null as WritableImage? }) })
        }

        var frame = this.cache!![point.x][point.y]

        if (frame == null)
        {
            val image = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
            val g = image.graphics as Graphics2D

            // clear background
            g.color = DrawingConstants.BACKGROUND_COLOR
            g.fillRect(0, 0, 32, 32)

            // draw layers
            for (layer in 0 until MargoMap.LAYERS)
            {
                if (this.drawOnlyCurrentLayer && this.currentLayer != layer)
                {
                    g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)
                }
                else
                {
                    g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
                }

                map.getFragment(point, layer)!!.draw(g)
            }
            g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)

            // draw grid
            if (this.shouldDrawGrid)
            {
                g.color = DrawingConstants.GRID_COLOR
                g.drawRect(0, 0, 32, 32)
            }

            // draw collisions
            if (this.shouldDrawCollisions && map.getCollisionAt(point))
            {
                g.color = DrawingConstants.COLLISION_BORDER
                g.drawRect(4, 4, 23, 23)
                g.color = DrawingConstants.COLLISION_FILL
                g.fillRect(5, 5, 22, 22)
            }

            // draw water
            if (this.shouldDrawWater)
            {
                val waterLevel = map.getWaterLevelAt(point)
                if (waterLevel > 0)
                {
                    g.color = DrawingConstants.WATER_BORDER
                    g.drawRect(4, 4, 23, 23)
                    g.color = DrawingConstants.WATER_FILL
                    g.fillRect(5, 5, 22, 22)
                    g.color = DrawingConstants.WATER_TEXT_COLOR

                    g.font = DrawingConstants.WATER_FONT
                    g.drawString(waterLevel.toString(), 10, 22)
                }
            }

            // draw objects

            for (mapObject in map.objects)
            {
                @Suppress("UNCHECKED_CAST")
                val tool = ObjectTools.getForRaw(mapObject) as? ObjectTool<MapObject<*>>

                if (tool != null)
                {
                    if (tool.contains(this.editor.mapEditor, mapObject, point))
                    {
                        tool.draw(this.editor.mapEditor, mapObject, g, point)
                    }
                }
            }

            // cache
            frame = SwingFXUtils.toFXImage(image, null)
            this.cache!![point.x][point.y] = frame
        }

        canvasG.drawImage(frame, (point.x - this.shiftX) * this.pointSize.toDouble(), (point.y - this.shiftY) * this.pointSize.toDouble(), this.pointSize.toDouble(), this.pointSize.toDouble())
    }

    fun resetWholeCache()
    {
        this.cache = null
    }

    fun resetCache(selection: Selection)
    {
        if (cache == null)
        {
            return
        }

        for (point in selection.points)
        {
            this.resetCache(point)
        }
    }

    fun resetCache(point: Point)
    {
        if (point.x >= 0 && point.y >= 0 && point.x < this.cache!!.size && point.y < this.cache!![point.x].size)
        {
            this.cache!![point.x][point.y] = null
        }
    }

    fun resetCacheAndRedraw(selection: Selection)
    {
        this.resetCache(selection)
        this.redrawFragment(selection)
    }

    fun smartRedraw(selection: Selection)
    {
        val smartSelection = HashSet<Point>()

        for (point in selection.points)
        {
            smartSelection.add(point)
            smartSelection.addAll(point.getNeighborhood(true))
        }

        this.resetCacheAndRedraw(Selection(smartSelection))
    }
}