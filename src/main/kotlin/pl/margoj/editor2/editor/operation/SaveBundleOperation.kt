package pl.margoj.editor2.editor.operation

import javafx.application.Platform
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.utils.javafx.operation.OperationCallback

class SaveBundleOperation(val editor: MargoJEditor) : SimpleOperation<SaveBundleOperation>()
{
    override val name: String = "Zapisywanie zestawu zasobów"

    override fun start0(operationCallback: OperationCallback<SaveBundleOperation>)
    {
        operationCallback.operationProgress(this, 0, -1)

        this.editor.bundle.saveBundle()

        Platform.runLater {
            QuickAlert.create().information().header("Zapisano").content("Zestaw zasobów został zapisany!").showAndWait()
        }
    }
}