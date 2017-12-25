package pl.margoj.editor2.app.controller.dialog.catalog

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.text.Text
import org.apache.commons.lang3.StringUtils
import pl.margoj.editor2.EditorApplication
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.app.controller.GraphicsController
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.operation.DeleteCatalogOperation
import java.net.URL
import java.util.ResourceBundle
import kotlin.collections.ArrayList

class CatalogController : CustomController
{
    private lateinit var editor: MargoJEditor
    private lateinit var listContent: MutableList<Text>
    lateinit var scene: CustomScene<*>
    lateinit var parent: GraphicsController

    @FXML
    lateinit var list: ListView<Text>

    @FXML
    lateinit var fieldSearch: TextField

    @FXML
    lateinit var buttonAddNew: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun loadData(data: Any)
    {
        data as GraphicsController

        this.parent = data
        this.editor = data.editor

        this.parent.catalogsWindow = this

        this.updateOptions(data.catalogs)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        this.fieldSearch.textProperty().addListener { _, _, _ -> this.updateView() }

        this.buttonAddNew.setOnAction {
            FXUtils.loadDialog(EditorApplication::class.java.classLoader, "catalog/newcatalog", "Nowy katalog", this.scene.stage, this)
        }
    }

    fun updateOptions(options: Map<String, String>)
    {
        this.listContent = ArrayList(options.size)

        for ((name, catalog) in options)
        {
            if (catalog.isEmpty())
            {
                continue
            }

            val deleteCallback: (Boolean) -> Unit = { success ->
                 Platform.runLater {
                    if (success)
                    {
                        QuickAlert.create().information().header("Katalog usunięty!").content("Katalog '$name' został pomyślnie usunięty").showAndWait()
                        this.parent.updateCatalogs()
                    }
                    else
                    {
                        QuickAlert.create().error().header("Nie można usunąć katalogu").content("Katalog nie jest pusty").showAndWait()
                    }
                }
            }

            val text = Text("$name [$catalog]")
            text.setOnMouseClicked { event ->
                if (event.button == MouseButton.PRIMARY && event.clickCount == 2)
                {
                    this.parent.choiceCatalog.selectionModel.select(name)
                    this.scene.stage.close()
                }

                if (event.button == MouseButton.SECONDARY)
                {
                    val contextMenu = ContextMenu()

                    val openItem = MenuItem("Otwórz katalog")
                    openItem.setOnAction {
                        this.parent.choiceCatalog.selectionModel.select(name)
                        this.scene.stage.close()
                    }

                    val deleteItem = MenuItem("Usuń katalog")
                    deleteItem.setOnAction {
                        this.editor.startOperation(DeleteCatalogOperation(this.editor, this.parent.category, name, deleteCallback))
                    }

                    contextMenu.items.setAll(openItem, deleteItem)

                    contextMenu.show(this.scene.stage, event.screenX, event.screenY)
                }
            }

            text.setOnKeyReleased {
                if (it.code == KeyCode.DELETE)
                {
                    this.editor.startOperation(DeleteCatalogOperation(this.editor, this.parent.category, name, deleteCallback))
                }
            }

            this.listContent.add(text)
        }

        this.updateView()
    }

    private fun updateView()
    {
        val searchTerm = this.fieldSearch.text
        val items = ArrayList<Text>()

        for (text in this.listContent)
        {
            if (StringUtils.containsIgnoreCase(text.text, searchTerm))
            {
                items.add(text)
            }
        }

        this.list.items.setAll(items)
    }
}