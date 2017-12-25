package pl.margoj.editor2.editor.map.operation

import javafx.application.Platform
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.operation.SimpleOperation
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.map.tileset.AutoTileset
import pl.margoj.mrf.map.tileset.Tileset
import pl.margoj.mrf.map.tileset.TilesetFile
import java.util.ArrayList
import java.util.Collections

class LoadTilesetOperation(val editor: MapEditor, val tilesetId: String) : SimpleOperation<LoadTilesetOperation>()
{
    override val name: String = "Ładowanie tilesetu: $tilesetId"

    override fun start0(operationCallback: OperationCallback<LoadTilesetOperation>)
    {
        val bundle = editor.editor.bundle

        val tileset: Tileset

        if (this.tilesetId == AutoTileset.AUTO)
        {
            val autos = ArrayList<TilesetFile>()

            for (tilesetResource in bundle.getResourcesByCategory(MargoResource.Category.TILESETS))
            {
                if (tilesetResource.id.startsWith("auto-"))
                {
                    autos.add(TilesetFile(bundle.loadResource(tilesetResource)!!, tilesetResource.id, true))
                }
            }

            tileset = AutoTileset(AutoTileset.AUTO, autos)
        }
        else
        {
            val resource = bundle.getResource(MargoResource.Category.TILESETS, this.tilesetId)!!
            val file = TilesetFile(bundle.loadResource(resource)!!, resource.id, false)
            tileset = Tileset(resource.id, file.image, Collections.singletonList(file))
        }

        Platform.runLater {
            this.editor.tilesetManager.currentTileset = tileset
        }
    }
}