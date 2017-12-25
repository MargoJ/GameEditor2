package pl.margoj.editor2.editor.operation

import javafx.application.Platform
import org.apache.commons.io.IOUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.mrf.MRFIcon
import pl.margoj.mrf.MRFIconFormat
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.graphics.GraphicResource
import pl.margoj.mrf.graphics.GraphicSerializer
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class AddGraphicsOperation(val editor: MargoJEditor, val category: GraphicResource.GraphicCategory, val catalog: String, val list: Collection<File>, val callback: () -> Unit) : SimpleOperation<AddGraphicsOperation>()
{
    override val name: String = "Dodawanie grafik"

    override fun start0(operationCallback: OperationCallback<AddGraphicsOperation>)
    {
        operationCallback.operationProgress(this, -1, 0)

        val serialzier = GraphicSerializer()

        val errors = ArrayList<String>()
        var progress = 0

        for (file in this.list)
        {
            operationCallback.operationProgress(this, progress, this.list.size)
            progress++

            val extension = file.extension
            val fileName = file.nameWithoutExtension

            val format = MRFIconFormat.values().find { it.extension == extension }
            if (format == null)
            {
                errors.add(" - ${file.name}: niepoprawne rozszerzenie: $extension")
                continue
            }

            val id = this.editor.getIconId(this.category, this.catalog, fileName, format)

            val existingGraphic = this.editor.bundle.getResource(MargoResource.Category.GRAPHIC, id)
            if (existingGraphic != null)
            {
                if (existingGraphic.meta?.has("catalog") == true)
                {
                    errors.add(" - ${file.name}: grafika już istnieje")
                    continue
                }
                else
                {
                    errors.add(" - ${file.name}: grafika została wysłana poprawnie ale zastąpiła inną grafikę o niepoprawnym katalogu")
                    this.editor.bundle.deleteResource(existingGraphic)
                }
            }

            var bytes: ByteArray? = null

            try
            {
                FileInputStream(file).use {
                    bytes = IOUtils.toByteArray(it)
                }
            }
            catch (e: IOException)
            {
                e.printStackTrace()
                errors.add(" - ${file.name}: $e")
                continue
            }

            val icon = MRFIcon(bytes!!, format, null)
            val resource = GraphicResource(id, "$fileName.${format.extension}", icon, this.category, this.catalog)

            this.editor.bundle.saveResource(resource, ByteArrayInputStream(serialzier.serialize(resource)))
        }

        progress++
        operationCallback.operationProgress(this, progress, this.list.size)

        Platform.runLater {
            if (errors.isEmpty())
            {
                QuickAlert.create()
                        .information()
                        .header("Grafiki wysłane!")
                        .content("Grafiki zostały wysłane poprawnie!")
                        .show()
            }
            else
            {
                QuickAlert.create()
                        .error()
                        .header("Podczas wysyłania wystąpiły następujące problemy")
                        .content(errors.joinToString("\n"))
                        .showAndWait()
            }

            this.callback()
        }
    }
}