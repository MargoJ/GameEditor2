package pl.margoj.editor2.editor.operation

import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.utils.javafx.operation.NotifyCancelOperation
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.data.DataConstants
import pl.margoj.mrf.data.DataDeserializer
import pl.margoj.mrf.data.GraphicCategoriesIndex
import pl.margoj.mrf.graphics.GraphicResource
import java.util.*
import kotlin.collections.LinkedHashMap

class LoadCatalogsOperation(val editor: MargoJEditor, val category: GraphicResource.GraphicCategory,  val callback: (MutableMap<String, String>?) -> Unit) : SimpleOperation<LoadCatalogsOperation>(), NotifyCancelOperation
{
    override val name: String = "Ładuje kategorie"

    override fun start0(operationCallback: OperationCallback<LoadCatalogsOperation>)
    {
        operationCallback.operationProgress(this, -1, 0)

        val map = LinkedHashMap<String, String>()
        map.put("Bez katalogu", "")

        val resource = this.editor.bundle.getResource(MargoResource.Category.DATA, DataConstants.GRAPHIC_CATEGORIES)

        if (resource == null)
        {
            callback(map)
            return
        }

        val stream = this.editor.bundle.loadResource(resource)!!
        val deserializer = DataDeserializer(DataConstants.GRAPHIC_CATEGORIES, GraphicCategoriesIndex::class.java)
        val index = deserializer.deserialize(stream).content.index[this.category.id.toString()] ?: Collections.emptyMap()

        for (key in index.keys.sorted())
        {
            map.put(key, index[key]!!)
        }

        callback(map)
    }

    override fun notifyCancel()
    {
        callback(null)
    }
}