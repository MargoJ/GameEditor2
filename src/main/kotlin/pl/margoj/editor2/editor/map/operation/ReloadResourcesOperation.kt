package pl.margoj.editor2.editor.map.operation

import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.operation.SimpleOperation
import pl.margoj.utils.javafx.operation.OperationCallback

class ReloadResourcesOperation(val editor: MargoJEditor) : SimpleOperation<ReloadResourcesOperation>()
{
    override val name: String = "Ładowanie listy zasobów"

    override fun start0(operationCallback: OperationCallback<ReloadResourcesOperation>)
    {
        operationCallback.operationProgress(this, 0, -1)

        val resources = editor.bundle.resources // TODO: Remote reload

        this.editor.gui.reloadResourcesView(resources)
    }
}