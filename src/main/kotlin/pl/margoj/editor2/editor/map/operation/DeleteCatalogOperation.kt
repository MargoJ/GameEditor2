package pl.margoj.editor2.editor.map.operation

import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.operation.OperationCallback
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.data.*
import pl.margoj.mrf.graphics.GraphicResource
import java.io.ByteArrayInputStream

class NewCatalogOperation(val editor: MargoJEditor, val category: GraphicResource.GraphicCategory, val catalogName: String, val catalog: String, val callback: () -> Unit) : SimpleOperation<NewCatalogOperation>()
{
    override val name: String = "Dodawanie nowego katalogu"

    override fun start0(operationCallback: OperationCallback<NewCatalogOperation>)
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

        index.content.index.computeIfAbsent(this.category.id.toString(), { hashMapOf() }).put(this.catalogName, this.catalog)

        val serializer = DataSerializer<GraphicCategoriesIndex>()
        this.editor.bundle.saveResource(index, ByteArrayInputStream(serializer.serialize(index)))

        this.callback()
    }
}