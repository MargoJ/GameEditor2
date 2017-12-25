package pl.margoj.editor2.editor.operation

import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.ResourceView
import pl.margoj.mrf.serialization.MRFDeserializer

class LoadResourceOperation<T : MargoResource>(val editor: MargoJEditor, val view: ResourceView, val deserializer: MRFDeserializer<T>, val callback: (T?) -> Unit) : SimpleOperation<LoadResourceOperation<T>>()
{
    override val name: String = "Ładowanie zasobu: ${view.resourceReadableName}"

    override fun start0(operationCallback: OperationCallback<LoadResourceOperation<T>>)
    {
        operationCallback.operationProgress(this, 0, -1)

        val stream =  this.editor.bundle.loadResource(this.view)
        if(stream == null)
        {
            this.callback(null)
            return
        }

        this.callback(this.deserializer.deserialize(stream))
    }
}