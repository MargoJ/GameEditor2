package pl.margoj.editor2.editor.map

import javafx.application.Platform
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import pl.margoj.editor2.EditorApplication
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.gui.map.MapGUI
import pl.margoj.editor2.editor.map.operation.LoadMapOperation
import pl.margoj.editor2.editor.map.operation.LoadTilesetOperation
import pl.margoj.editor2.editor.map.operation.SaveMapOperation
import pl.margoj.editor2.editor.map.render.EditorMapRenderer
import pl.margoj.editor2.editor.map.tileset.TilesetManager
import pl.margoj.editor2.editor.map.tools.*
import pl.margoj.editor2.editor.map.undo.ObjectChangeAction
import pl.margoj.editor2.editor.map.undo.UndoAction
import pl.margoj.editor2.editor.map.utils.NpcImage
import pl.margoj.editor2.geometry.RectangleSelection
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.MapObject
import pl.margoj.mrf.map.tileset.Tileset
import java.util.*
import kotlin.collections.HashMap

class MapEditor(val editor: MargoJEditor)
{
    private var undoActions = LinkedList<UndoAction>()
    private var redoActions = LinkedList<UndoAction>()

    val renderer = EditorMapRenderer(this)

    val tilesetManager = TilesetManager(this)

    val gui: MapGUI get() = this.editor.gui.mapGUI

    var npcCache = HashMap<String, NpcImage>()

    var currentMap: MargoMap? = null
        set(value)
        {
            field = value

            if (value != null)
            {
                this.selection = RectangleSelection(0, 0, value.width, value.height)
            }

            if (value !== field)
            {
                this.clearUndoRedoHistory()
            }

            this.touched = false
            this.editor.gui.mapGUI.update()
            this.renderer.resetWholeCache()
            this.renderer.resizeIfNeeded()
        }

    var currentLayer: Int = 0
        set(value)
        {
            if (field == value)
            {
                return
            }

            val previous = field
            field = value

            if (this.drawOnlyCurrentLayer || previous == MargoMap.COLLISION_LAYER || value == MargoMap.COLLISION_LAYER || previous == MargoMap.WATER_LAYER || value == MargoMap.WATER_LAYER)
            {
                this.renderer.resetWholeCache()
                this.renderer.redrawAll()
            }
        }

    var tools = arrayOf(ToolSelect(this), SingleElementTool(this), EraserTool(this), FillingTool(this), ObjectTool(this))

    var currentTool: Tool = this.tools[1]

    var showGrid = false
        set(value)
        {
            field = value
            this.renderer.resetWholeCache()
            this.renderer.redrawAll()
        }

    var drawOnlyCurrentLayer = false
        set(value)
        {
            field = value
            this.renderer.resetWholeCache()
            this.renderer.redrawAll()
        }

    lateinit var selection: Selection

    var touched = false

    fun init()
    {
        this.tilesetManager.init()
    }

    fun requestNewMap()
    {
        this.askForSaveIfNecessary()

        FXUtils.loadDialog(EditorApplication::class.java.classLoader, "map/new", "Dodaj nową mape", this.gui.editorGUI.scene.stage, this.editor)
    }

    fun selectTileset(id: String)
    {
        this.editor.startOperation(LoadTilesetOperation(this, id))
    }

    fun selectMap(id: String)
    {
        if(!this.askForSaveIfNecessary())
        {
            return
        }

        this.loadMap(id) { map ->
            Platform.runLater {
                this.currentMap = map
            }
        }
    }

    fun selectTool(id: Int)
    {
        this.currentTool = this.tools[id]
    }

    fun canvasPointToRendererPoint(point: Point): Point
    {
        return point.getRelative(this.renderer.shiftX, this.renderer.shiftY)
    }

    fun addUndoAction(undoAction: UndoAction)
    {
        this.touched = true

        this.undoActions.add(undoAction)
        this.redoActions.clear()
    }

    fun clearUndoRedoHistory()
    {
        this.undoActions.clear()
        this.redoActions.clear()
    }

    fun undo()
    {
        val last = this.undoActions.pollLast()
        if (last == null)
        {
            QuickAlert.create().warning().header("Nie udało się cofnąć").content("Nie ma nic do cofnięcia").showAndWait()
            return
        }
        last.undo()
        this.redoActions.add(last)
    }

    fun redo()
    {
        val last = this.redoActions.pollLast()
        if (last == null)
        {
            QuickAlert.create().warning().header("Nie udało się powtórzyć").content("Nie ma nic do powtórzenia").showAndWait()
            return
        }
        last.redo()
        this.undoActions.add(last)
    }

    fun askForSaveIfNecessary(): Boolean
    {
        if (this.touched)
        {
            val result = QuickAlert.create()
                    .confirmation()
                    .header("Nie zapisałeś ostatnich zmian.")
                    .content("Czy chcesz zapisać mape?")
                    .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO), ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE))
                    .showAndWait()

            return when (result?.buttonData)
            {
                ButtonBar.ButtonData.YES ->
                {
                    this.saveMap()
                    false
                }
                ButtonBar.ButtonData.NO -> true
                else -> false
            }
        }

        return true
    }

    fun saveMap()
    {
        if (this.currentMap != null)
        {
            this.editor.startOperation(SaveMapOperation(this, this.currentMap!!))
        }
    }

    fun loadTilesets(): Collection<Tileset>
    {
        return this.editor.bundle.bundleOperation.loadTilesets()
    }

    fun editMap()
    {
        if (this.currentMap != null)
        {
            FXUtils.loadDialog(EditorApplication::class.java.classLoader, "map/new", "Edytuj mape", this.gui.editorGUI.scene.stage, Pair(this.editor, this.currentMap))
        }
    }

    fun editMeta()
    {
        if (this.currentMap != null)
        {
            FXUtils.loadDialog(EditorApplication::class.java.classLoader, "map/editmeta", "Edytuj dane mapy", this.gui.editorGUI.scene.stage, Pair(this, this.currentMap))
        }
    }

    fun setObject(point: Point, mapObject: MapObject<*>?)
    {
        val currentPoint = this.currentMap?.getObject(point)
        val action = ObjectChangeAction(this, currentPoint, mapObject)
        action.redo()

        this.addUndoAction(action)
    }

    fun openPreview(map: MargoMap)
    {
        val stage = FXUtils.loadDialog(EditorApplication::class.java.classLoader, "map/preview", "Podgląd mapy: ${map.name} [${map.id}]", this.gui.editorGUI.scene.stage, Triple(this.editor, map, null))
        stage.isResizable = true
    }

    fun openSelect(map: MargoMap, title: String, callback: (Point) -> Unit)
    {
        val stage = FXUtils.loadDialog(EditorApplication::class.java.classLoader, "map/preview", title, this.gui.editorGUI.scene.stage, Triple(this.editor, map, callback))
        stage.isResizable = true
    }

    fun loadMap(id: String, callback: (MargoMap?) -> Unit)
    {
        this.editor.startOperation(LoadMapOperation(this, id, callback))
    }
}