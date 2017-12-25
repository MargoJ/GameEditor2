package pl.margoj.editor2.editor.map.operation

import javafx.application.Platform
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.operation.SimpleOperation
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.serialization.MapSerializer
import java.io.ByteArrayInputStream

class SaveMapOperation(val editor: MapEditor, val map: MargoMap) : SimpleOperation<SaveMapOperation>()
{
    override val name: String = "Zapisywanie mapy: ${map.name}"

    override fun start0(operationCallback: OperationCallback<SaveMapOperation>)
    {
        operationCallback.operationProgress(this, 0, -1)

        val bundle = this.editor.editor.bundle

        val serializer = MapSerializer()
        val mapBytes = serializer.serialize(map)

        map.size = mapBytes.size

        val newMap = bundle.getResource(MargoResource.Category.MAPS, map.id) == null
        bundle.saveResource(map, ByteArrayInputStream(mapBytes))

        this.editor.touched = false

        Platform.runLater {
            QuickAlert.create().information().header("Zapisano").content("Mapa: ${map.name} została zapisana poprawnie!").showAndWait()

            if (newMap)
            {
                this.editor.editor.reloadResourceIndex()
            }
        }
    }
}