package pl.margoj.editor2.editor.item.operation

import com.google.gson.JsonPrimitive
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Scene
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.stage.Modality
import javafx.stage.Stage
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.operation.SimpleOperation
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.editor2.utils.ProgressReportingInputStream
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.graphics.GraphicDeserializer
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class ShowIconPreviewOperation(val editor: MargoJEditor, val id: String, val parentWindow: Stage? = null) : SimpleOperation<ShowIconPreviewOperation>()
{
    override val name: String = "Ładowanie podglądu: $id"

    override fun start0(operationCallback: OperationCallback<ShowIconPreviewOperation>)
    {
        operationCallback.operationProgress(this, 0, -1)

        val resource = this.editor.bundle.getResource(MargoResource.Category.GRAPHIC, this.id)

        if (resource == null)
        {
            Platform.runLater {
                QuickAlert.create().error().header("Nie znaleziono grafiki").content("Zasób '$id' nie został znaleziony w zestawie").showAndWait()
            }

            return
        }

        val sizePrimitive = resource.meta?.get("size") as? JsonPrimitive
        val size = if (sizePrimitive?.isNumber == true) sizePrimitive.asNumber.toInt() else -1

        operationCallback.operationProgress(this, 0, size)

        val stream = ProgressReportingInputStream(this.editor.bundle.loadResource(resource)!!) { progress ->
            operationCallback.operationProgress(this, progress.toInt(), size)
        }

        val graphicDeserializer = GraphicDeserializer()
        val graphics = graphicDeserializer.deserialize(stream)

        val image = SwingFXUtils.toFXImage(ImageIO.read(ByteArrayInputStream(graphics.icon.image)), null)

        Platform.runLater {
            val parent = BorderPane()
            parent.setMinSize(500.0, 500.0)
            parent.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
            parent.center = ImageView(image)

            val scene = Scene(parent)
            val stage = Stage()

            if (this.parentWindow != null)
            {
                stage.initModality(Modality.WINDOW_MODAL)
                stage.initOwner(this.parentWindow)
            }

            stage.scene = scene
            stage.title = "Podgląd grafiki: ${graphics.fileName}"
            stage.minWidth = 500.0
            stage.minHeight = 500.0
            FXUtils.setStageIcon(stage, "icon.png")
            stage.show()
        }
    }
}