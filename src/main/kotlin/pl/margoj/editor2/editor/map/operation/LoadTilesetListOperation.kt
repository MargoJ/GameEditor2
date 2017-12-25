package pl.margoj.editor2.editor.map.operation

import javafx.application.Platform
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.operation.SimpleOperation
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.map.tileset.AutoTileset

class LoadTilesetListOperation(val editor: MapEditor) : SimpleOperation<LoadTilesetListOperation>()
{
    override val name: String = "Ładowanie tilesetów"

    override fun start0(operationCallback: OperationCallback<LoadTilesetListOperation>)
    {
        val bundle = editor.editor.bundle

        operationCallback.operationProgress(this, -1, 0)

        val resources = bundle.getResourcesByCategory(MargoResource.Category.TILESETS)

        val possibilities = ArrayList<String>()
        possibilities.add(AutoTileset.AUTO)
        resources.filter { !it.id.startsWith("auto-") }.mapTo(possibilities) { it.id }

        Platform.runLater {
            this.editor.editor.gui.showSelectDialog("Wybierz tileset", possibilities) { response ->
                this.editor.selectTileset(response)
            }
        }
    }
}