package pl.margoj.editor2.editor.map.operation

import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.operation.NotifyCancelOperation
import pl.margoj.editor2.operation.OperationCallback
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.data.DataConstants
import pl.margoj.mrf.data.DataDeserializer
import pl.margoj.mrf.data.GraphicCategoriesIndex

class LoadCatalogsOperation(val editor: MargoJEditor, val callback: (Map<String, String>?) -> Unit) : SimpleOperation<LoadCatalogsOperation>(), NotifyCancelOperation
{
    override val name: String = "Ładuje kategorie"

    override fun start0(operationCallback: OperationCallback<LoadCatalogsOperation>)
    {
        operationCallback.operationProgress(this, -1, 0)

        val map = HashMap<String, String>()
        map.put("Bez katalogu", "")

        val resource = this.editor.bundle.getResource(MargoResource.Category.DATA, DataConstants.GRAPHIC_CATEGORIES)

        if (resource == null)
        {
            callback(map)
            return
        }

        val stream = this.editor.bundle.loadResource(resource)!!
        val deserializer = DataDeserializer(DataConstants.GRAPHIC_CATEGORIES, GraphicCategoriesIndex::class.java)
        val index = deserializer.deserialize(stream).content.index

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