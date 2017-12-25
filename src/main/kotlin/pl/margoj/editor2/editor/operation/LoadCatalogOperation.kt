package pl.margoj.editor2.editor.operation

import javafx.embed.swing.SwingFXUtils
import javafx.scene.Node
import pl.margoj.editor2.EditorApplication
import pl.margoj.editor2.app.controller.parts.GraphicController
import pl.margoj.utils.javafx.utils.FXMLJarLoader
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.graphics.GraphicDeserializer
import pl.margoj.mrf.graphics.GraphicResource
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class LoadCatalogOperation(val editor: MargoJEditor, val category: GraphicResource.GraphicCategory, val catalog: String, val callback: (Collection<Node>) -> Unit) : SimpleOperation<LoadCatalogOperation>()
{
    override val name: String = "Ładuje kategorie"

    override fun start0(operationCallback: OperationCallback<LoadCatalogOperation>)
    {
        operationCallback.operationProgress(this, -1, 0)

        val deserializer = GraphicDeserializer()

        val items = ArrayList<Node>()

        val resourceViews = this.editor.bundle.getResourcesByCategory(MargoResource.Category.GRAPHIC)
        var progress = 0
        operationCallback.operationProgress(this, 0, resourceViews.size)

        for (resourceView in resourceViews)
        {
            operationCallback.operationProgress(this, progress, resourceViews.size)
            progress++

            val meta = resourceView.view.meta

            if (meta != null && meta.has("cat") && meta.get("cat").asByte != this.category.id)
            {
                continue
            }

            if (this.catalog.isEmpty())
            {
                if (meta != null && meta.has("catalog"))
                {
                    continue
                }
            }
            else
            {
                if (meta == null || !meta.has("catalog") || meta.get("catalog").asString != this.catalog)
                {
                    continue
                }
            }

            val graphicStream = this.editor.bundle.loadResource(resourceView)!!
            val graphic = deserializer.deserialize(graphicStream)

            val image = SwingFXUtils.toFXImage(ImageIO.read(ByteArrayInputStream(graphic.icon.image)), null)

            val loader = FXMLJarLoader(EditorApplication::class.java.classLoader, "parts/graphic")
            loader.load()

            val controller = loader.controller as GraphicController
            controller.labelName.text = graphic.fileName
            controller.imageView.image = image
            controller.imageView.fitWidth = image.width
            controller.imageView.fitHeight = image.height

            loader.node.properties.put(GRAPHIC_NODE, graphic)
            items.add(loader.node)
        }

        operationCallback.operationProgress(this, progress, resourceViews.size)

        this.callback(items)
    }

    companion object
    {
        val GRAPHIC_NODE = "mjeditor-node-graphic"
    }
}