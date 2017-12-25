package pl.margoj.editor2.app.controller

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import pl.margoj.editor2.EditorApplication
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.app.controller.dialog.catalog.CatalogController
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.operation.AddGraphicsOperation
import pl.margoj.editor2.editor.operation.DownloadGraphicsOperation
import pl.margoj.editor2.editor.operation.LoadCatalogOperation
import pl.margoj.editor2.editor.operation.LoadCatalogsOperation
import pl.margoj.mrf.graphics.GraphicResource
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class GraphicsController : CustomController
{
    private var fileChooser = FileChooser()
    private val directoryChooser = DirectoryChooser()

    lateinit var scene: CustomScene<*>
    lateinit var editor: MargoJEditor
    lateinit var category: GraphicResource.GraphicCategory
    lateinit var catalogs: MutableMap<String, String>
    lateinit var callback: (GraphicResource) -> Unit
    var catalogsWindow: CatalogController? = null
    val currentSelection = HashSet<Node>()

    @FXML
    lateinit var container: Pane

    @FXML
    lateinit var buttonAddNew: Button

    @FXML
    lateinit var choiceCatalog: ComboBox<String>

    @FXML
    lateinit var buttonCatalog: Button

    @FXML
    lateinit var buttonOk: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun loadData(data: Any)
    {
        @Suppress("UNCHECKED_CAST")
        data as Triple<MargoJEditor, GraphicResource.GraphicCategory, (GraphicResource) -> Unit>

        this.editor = data.first
        this.category = data.second
        this.callback = data.third

        this.updateCatalogs()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        this.buttonCatalog.setOnAction {
            FXUtils.loadDialog(EditorApplication::class.java.classLoader, "catalog/catalogs", "Lista katalogów", this.scene.stage, this)
        }

        this.choiceCatalog.selectionModel.selectedItemProperty().addListener { _, oldValue, newValue ->
            if (oldValue != newValue)
            {
                this.loadFromCatalog(newValue)
            }
        }

        this.fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Wspierane formaty grafik", "*.png", "*.gif"))

        this.buttonAddNew.setOnAction {
            val catalog = this.catalogs[this.choiceCatalog.selectionModel.selectedItem]
            if (catalog == null || catalog.isEmpty())
            {
                Platform.runLater {
                    QuickAlert.create().error().header("Nie można dodać grafik").content("Musisz najpierw wybrać katalog").showAndWait()
                }
                return@setOnAction
            }

            val result = this.fileChooser.showOpenMultipleDialog(this.scene.stage) ?: return@setOnAction

            this.editor.startOperation(AddGraphicsOperation(this.editor, this.category, catalog, result, {
                this.loadFromCatalog()
            }))
        }

        this.buttonOk.setOnAction {
            if (this.currentSelection.size != 1)
            {
                QuickAlert.create().error().header("Nie można ustawić grafiki").content("Musisz wybrać jedną grafikę").showAndWait()
                return@setOnAction
            }

            this.selected(this.currentSelection.first())
        }
    }

    fun updateCatalogs()
    {
        this.editor.startOperation(LoadCatalogsOperation(this.editor, this.category, { catalogs ->
            if (catalogs == null)
            {
                Platform.runLater {
                    this.scene.stage.close()
                }
                return@LoadCatalogsOperation
            }

            Platform.runLater {
                val empty = this.choiceCatalog.items.isEmpty()
                this.catalogs = catalogs

                this.choiceCatalog.items.setAll(catalogs.keys)

                if (this.catalogsWindow?.scene?.stage?.isShowing == true)
                {
                    this.catalogsWindow!!.updateOptions(catalogs)
                }

                if (empty || !catalogs.contains(this.choiceCatalog.selectionModel.selectedItem))
                {
                    this.choiceCatalog.selectionModel.select(0)
                    this.loadFromCatalog()
                }
            }
        }))
    }

    private fun loadFromCatalog(catalog: String = this.choiceCatalog.selectionModel.selectedItem)
    {
        this.editor.startOperation(LoadCatalogOperation(this.editor, this.category, this.catalogs[catalog] ?: "", { nodes ->
            this.currentSelection.clear()

            Platform.runLater {
                this.container.children.setAll(nodes)

                for (node in nodes)
                {
                    node.setOnMouseClicked { event ->
                        if (!event.isShiftDown)
                        {
                            if (event.button != MouseButton.SECONDARY || !currentSelection.contains(node))
                            {
                                for (selection in currentSelection)
                                {
                                    selection.style = "-fx-border-color: transparent;"
                                }

                                currentSelection.clear()
                            }
                        }

                        if (currentSelection.contains(node) && event.isShiftDown)
                        {
                            node.style = "-fx-border-color: transparent;"
                            currentSelection.remove(node)
                        }
                        else
                        {
                            node.style = "-fx-border-color: #4286f4;"
                            currentSelection.add(node)
                        }

                        if (event.button == MouseButton.PRIMARY && event.clickCount == 2)
                        {
                            this.selected(node)
                        }
                        else if (event.button == MouseButton.SECONDARY)
                        {
                            val contextMenu = ContextMenu()

                            val openItem = MenuItem("Otwórz grafikę")
                            openItem.setOnAction {
                                this.selected(node)
                            }

                            val downloadItem = MenuItem("Pobierz grafikę")
                            downloadItem.setOnAction {
                                val directory = this.directoryChooser.showDialog(this.scene.stage) ?: return@setOnAction

                                val resources = ArrayList<GraphicResource>()
                                currentSelection.mapTo(resources) { it.properties[LoadCatalogOperation.GRAPHIC_NODE] as GraphicResource }

                                this.editor.startOperation(DownloadGraphicsOperation(this.editor, directory, resources))
                            }

                            val deleteItem = MenuItem("Usuń grafikę")
                            deleteItem.setOnAction {
                                val result = QuickAlert.create()
                                        .confirmation()
                                        .header("Czy na pewno chces usunac zasób?")
                                        .content("Czy na pewno chcesz usunąć wybrane ${currentSelection.size} elementów?")
                                        .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO))
                                        .showAndWait()

                                if (result?.buttonData == ButtonBar.ButtonData.YES)
                                {
                                    for (selectionNode in currentSelection)
                                    {
                                        this.editor.bundle.deleteResource((selectionNode.properties[LoadCatalogOperation.GRAPHIC_NODE] as GraphicResource).view)
                                    }

                                    this.loadFromCatalog()
                                }
                            }

                            contextMenu.items.setAll(openItem, downloadItem, deleteItem)

                            contextMenu.show(this.scene.stage, event.screenX, event.screenY)
                        }
                    }
                }
            }
        }))
    }

    private fun selected(node: Node)
    {
        val graphicResource = node.properties[LoadCatalogOperation.GRAPHIC_NODE] as GraphicResource
        this.callback(graphicResource)
        this.scene.stage.close()
    }
}