package pl.margoj.editor2.editor.operation

import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.data.*
import pl.margoj.mrf.graphics.GraphicResource
import java.io.ByteArrayInputStream

class DeleteCatalogOperation(val editor: MargoJEditor, val category: GraphicResource.GraphicCategory, val catalogName: String, val callback: (Boolean) -> Unit) : SimpleOperation<DeleteCatalogOperation>()
{
    override val name: String = "Usuwanie katalogu"

    override fun start0(operationCallback: OperationCallback<DeleteCatalogOperation>)
    {
        val resource = this.editor.bundle.getResource(MargoResource.Category.DATA, DataConstants.GRAPHIC_CATEGORIES)
        val index: DataResource<GraphicCategoriesIndex>

        if (resource == null)
        {
            index = DataResource(DataConstants.GRAPHIC_CATEGORIES)
            index.content = GraphicCategoriesIndex()
        }
        else
        {
            val stream = this.editor.bundle.loadResource(resource)!!
            val deserializer = DataDeserializer(DataConstants.GRAPHIC_CATEGORIES, GraphicCategoriesIndex::class.java)
            index = deserializer.deserialize(stream)
        }

        val catalog = index.content.index[this.category.id.toString()]?.get(this.catalogName)

        if (catalog != null)
        {
            for (resourceView in this.editor.bundle.getResourcesByCategory(MargoResource.Category.GRAPHIC))
            {
                if (resourceView.meta != null && resourceView.meta!!.has("catalog") && resourceView.meta!!.get("catalog").asString == catalog && resourceView.meta!!.has("cat") && resourceView.meta!!.get("cat").asByte == this.category.id)
                {
                    this.callback(false)
                    return
                }
            }

            index.content.index[this.category.id.toString()]?.remove(this.catalogName)

            val serializer = DataSerializer<GraphicCategoriesIndex>()
            this.editor.bundle.saveResource(index, ByteArrayInputStream(serializer.serialize(index)))
        }

        this.callback(true)
    }
}