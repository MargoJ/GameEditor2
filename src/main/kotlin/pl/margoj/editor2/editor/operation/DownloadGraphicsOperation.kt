package pl.margoj.editor2.editor.operation

import javafx.application.Platform
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.mrf.graphics.GraphicResource
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream

class DownloadGraphicsOperation(val editor: MargoJEditor, val directory: File, val resources: ArrayList<GraphicResource>) : SimpleOperation<DownloadGraphicsOperation>()
{
    override val name: String = "Pobieranie grafiki"

    override fun start0(operationCallback: OperationCallback<DownloadGraphicsOperation>)
    {
        var progess = 0

        for (resource in this.resources)
        {
            var `try` = 0
            var file: File

            do
            {
                val name = StringBuilder(resource.fileName.removeSuffix("." + resource.icon.format.extension))

                if (`try` != 0)
                {
                    name.append(" (").append(`try`).append(")")
                }

                name.append(".").append(resource.icon.format.extension)

                file = File(this.directory, name.toString())
                `try`++
            }
            while (file.exists())

            FileOutputStream(file).use {
                it.write(resource.icon.image)
            }

            operationCallback.operationProgress(this, progess, this.resources.size)
            progess++
        }

        operationCallback.operationProgress(this, progess, this.resources.size)

        Platform.runLater {
            try
            {
                Desktop.getDesktop().open(this.directory)
            }
            catch (e: UnsupportedOperationException)
            {
                e.printStackTrace()
            }
        }
    }
}