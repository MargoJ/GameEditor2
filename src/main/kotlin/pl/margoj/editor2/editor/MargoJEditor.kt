package pl.margoj.editor2.editor

import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.stage.Modality
import javafx.stage.Stage
import pl.margoj.editor2.EditorApplication
import pl.margoj.editor2.app.scene.GraphicsScene
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.gui.EditorGUI
import pl.margoj.editor2.editor.item.ItemEditor
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.operation.ReloadResourcesOperation
import pl.margoj.editor2.editor.npc.NpcEditor
import pl.margoj.editor2.editor.operation.LoadGameSettingsOperation
import pl.margoj.editor2.editor.operation.SaveBundleOperation
import pl.margoj.editor2.editor.script.ScriptEditor
import pl.margoj.utils.javafx.operation.Operation
import pl.margoj.editor2.utils.FileUtils
import pl.margoj.mrf.MRFIconFormat
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.bundle.MargoResourceBundle
import pl.margoj.mrf.graphics.GraphicResource

class MargoJEditor(val debuggingProfile: DebuggingProfile, val bundle: MargoResourceBundle)
{
    val gui = EditorGUI(this)
    val mapEditor = MapEditor(this)
    val itemEditor = ItemEditor(this)
    val npcEditor = NpcEditor(this)
    val scriptEditor = ScriptEditor(this)

    fun init()
    {
        FileUtils.ensureDirectoryCreationIfAvailable(FileUtils.PROGRAM_DIRECTORY)
        FileUtils.ensureDirectoryCreationIfAvailable(FileUtils.MOUNT_DIRECTORY)
        FileUtils.ensureDirectoryCreationIfAvailable(FileUtils.TEMP_DIRECTORY)

        this.reloadResourceIndex()
        this.mapEditor.init()
        this.itemEditor.init()
    }

    fun startOperation(operation: Operation<*>, stage: Stage = this.gui.scene.stage)
    {
        FXUtils.startOperation(operation, stage)
    }

    fun reloadResourceIndex()
    {
        this.startOperation(ReloadResourcesOperation(this))
    }

    fun askForSaveIfNecessary(): Boolean
    {
        if (this.bundle.touched)
        {
            val result = QuickAlert.create()
                    .confirmation()
                    .header("Nie zapisałeś ostatnich zmian.")
                    .content("Czy chcesz zapisać zestaw zasobów?")
                    .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO), ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE))
                    .showAndWait()

            return when (result?.buttonData)
            {
                ButtonBar.ButtonData.YES ->
                {
                    this.saveBundle()
                    false
                }
                ButtonBar.ButtonData.NO -> true
                else -> false
            }
        }

        return true
    }

    fun saveBundle()
    {
        this.startOperation(SaveBundleOperation(this))
    }

    fun deleteResource(category: MargoResource.Category, id: String)
    {
        val result = QuickAlert.create()
                .confirmation()
                .header("Czy na pewno chces usunac zasób?")
                .content("Czy na pewno chcesz usunać zasób  \"${category.id}/$id\"?")
                .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO))
                .showAndWait()

        if (result?.buttonData == ButtonBar.ButtonData.YES)
        {
            val view = this.bundle.getResource(category, id)
            if (view == null)
            {
                QuickAlert.create().error().header("Błąd usuwania zasobu!").content("Podany zasób nieznaleziony!").showAndWait()
                return
            }

            this.bundle.deleteResource(view)
            QuickAlert.create().information().header("Usunięto!").content("Usunięto wybrany zasób!").showAndWait()

            this.reloadResourceIndex()
        }
    }

    fun openGraphicsChoice(category: GraphicResource.GraphicCategory, callback: (GraphicResource) -> Unit)
    {
        val scene = GraphicsScene(this, category, callback)
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)

        if (category == GraphicResource.GraphicCategory.ITEM && this.itemEditor.scene != null)
        {
            stage.initOwner(this.itemEditor.scene!!.stage)
        }
        else
        {
            stage.initOwner(this.gui.controller.scene.stage)
        }

        scene.stage = stage
        scene.load(EditorApplication::class.java.classLoader)
        stage.show()
    }

    fun createIconText(resource: GraphicResource): String
    {
        val builder = StringBuilder()
        if (resource.catalog != null && resource.catalog!!.isNotEmpty())
        {
            builder.append(resource.catalog!!).append('/')
        }
        builder.append(resource.fileName)
        return builder.toString()
    }

    fun getIconId(category: GraphicResource.GraphicCategory, catalog: String, id: String, format: MRFIconFormat): String
    {
        val builder = StringBuilder()
        builder.append(category.id).append("_")
        if (catalog.isNotEmpty())
        {
            builder.append(catalog).append("_")
        }
        builder.append(id).append("_")
        builder.append(format.format)
        return builder.toString()
    }


    fun iconPathToIconId(category: GraphicResource.GraphicCategory, path: String): String
    {
        var rest = path
        val catalog: String
        if (rest.contains("/"))
        {
            catalog = path.substring(0, path.indexOf("/"))
            rest = path.substring(path.indexOf("/") + 1)
        }
        else
        {
            catalog = ""
        }

        val id = rest.substring(0, rest.lastIndexOf("."))
        val format = rest.substring(rest.lastIndexOf(".") + 1)

        return this.getIconId(category, catalog, id, MRFIconFormat.getByFormat(format)!!)
    }

    fun openGameSettings()
    {
        this.startOperation(LoadGameSettingsOperation(this))
    }
}