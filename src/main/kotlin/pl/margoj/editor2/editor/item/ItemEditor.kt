package pl.margoj.editor2.editor.item

import javafx.application.Platform
import javafx.stage.Modality
import javafx.stage.Stage
import pl.margoj.editor2.EditorApplication
import pl.margoj.editor2.app.scene.ItemScene
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.item.operation.ShowIconPreviewOperation
import pl.margoj.editor2.editor.operation.LoadResourceOperation
import pl.margoj.mrf.ResourceView
import pl.margoj.mrf.item.MargoItem
import pl.margoj.mrf.item.serialization.ItemDeserializer

class ItemEditor(val editor: MargoJEditor)
{
    val propertiesRenderer = PropertiesRenderer(this.editor, PropertiesRenderer.DEFAULT_PROPERTIES_RENDERERS.map { it() })

    var scene: ItemScene? = null

    fun init()
    {
        this.propertiesRenderer.calculate()
    }

    fun edit(resourceView: ResourceView)
    {
        this.editor.startOperation(LoadResourceOperation(this.editor, resourceView, ItemDeserializer(), { item ->
            Platform.runLater {
                this.edit(item!!)
            }
        }))
    }

    fun edit(item: MargoItem)
    {
        val scene = ItemScene(this, item)
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.initOwner(this.editor.gui.controller.scene.stage)
        scene.stage = stage
        scene.load(EditorApplication::class.java.classLoader)
        stage.show()

        this.scene = scene
    }

    fun requestNewItem()
    {
        FXUtils.loadDialog(EditorApplication::class.java.classLoader, "item/new", "Dodaj nowy przedmiot", this.editor.gui.scene.stage, this.editor)
    }

    fun showIconPreview(id: String)
    {
        this.editor.startOperation(ShowIconPreviewOperation(this.editor, id, this.scene?.stage))
    }
}