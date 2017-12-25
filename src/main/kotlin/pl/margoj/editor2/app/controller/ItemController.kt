package pl.margoj.editor2.app.controller

import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.item.ItemEditor
import pl.margoj.editor2.editor.item.renderer.ItemPropertyRenderer
import pl.margoj.mrf.item.ItemProperty
import pl.margoj.mrf.item.MargoItem
import pl.margoj.mrf.item.serialization.ItemSerializer
import java.io.ByteArrayInputStream
import java.net.URL
import java.util.*

class ItemController : CustomController
{
    private lateinit var editor: ItemEditor
    private lateinit var item: MargoItem

    @FXML
    lateinit var container: VBox

    @FXML
    lateinit var fieldSearch: TextField

    @FXML
    lateinit var buttonSave: Button

    @Suppress("UNCHECKED_CAST")
    override fun loadData(data: Any)
    {
        @Suppress("UNCHECKED_CAST")
        data as Pair<ItemEditor, MargoItem>

        this.editor = data.first
        this.item = data.second

        val propertiesRenderer = this.editor.propertiesRenderer
        this.update("")

        ItemProperty.properties
                .filter { it.editable }
                .forEach { property ->
                    property as ItemProperty<Any>
                    val renderer = propertiesRenderer.getRendererOf(property)!! as ItemPropertyRenderer<Any, ItemProperty<Any>, Node>
                    val node = propertiesRenderer.actualNodes[property]!!

                    renderer.update(property, node, this.item[property])
                }
    }

    override fun preInit(scene: CustomScene<*>)
    {
        scene.stage.setOnCloseRequest {
            val result = QuickAlert.create()
                    .confirmation()
                    .header("Czy chcesz zapisać?")
                    .content("Czy chcesz zapisać aktualny przedmiot?")
                    .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO), ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE))
                    .showAndWait()

            when (result?.buttonData)
            {
                ButtonBar.ButtonData.YES ->
                {
                    this.buttonSave.fire()
                }
                ButtonBar.ButtonData.NO ->
                {
                }
                else -> it.consume()
            }

            if(!it.isConsumed)
            {
                this.editor.scene = null
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        this.fieldSearch.textProperty().addListener { _, _, newValue ->
            this.update(newValue)
        }

        this.buttonSave.setOnAction {
            val propertiesRenderer = this.editor.propertiesRenderer

            for (property in ItemProperty.properties)
            {
                if(!property.editable)
                {
                    continue
                }

                property as ItemProperty<Any>
                val renderer = propertiesRenderer.getRendererOf(property)!! as ItemPropertyRenderer<Any, ItemProperty<Any>, Node>
                val node = propertiesRenderer.actualNodes[property]!!

                val returned = renderer.convert(property, node) ?: return@setOnAction

                if (returned == property.default)
                {
                    continue
                }

                this.item[property] = returned
            }

            this.editor.editor.bundle.saveResource(this.item, ByteArrayInputStream(ItemSerializer().serialize(this.item)))
            this.editor.editor.reloadResourceIndex()

            QuickAlert.create().information().header("Zapisano!").content("Przedmiot: ${this.item.id} został zapisany poprawnie!").showAndWait()
        }
    }

    private fun update(search: String)
    {
        this.editor.propertiesRenderer.render(this.container, search)
    }
}