package pl.margoj.editor2.editor.map.operation

import com.google.gson.JsonPrimitive
import org.apache.commons.io.IOUtils
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.utils.NpcImage
import pl.margoj.editor2.editor.operation.SimpleOperation
import pl.margoj.editor2.utils.ProgressReportingInputStream
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.ResourceView
import pl.margoj.mrf.graphics.GraphicDeserializer
import pl.margoj.mrf.graphics.GraphicResource
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.objects.npc.NpcMapObject
import pl.margoj.mrf.map.serialization.MapDeserializer
import pl.margoj.mrf.npc.NpcDeserializer
import pl.margoj.utils.javafx.operation.OperationCallback
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class LoadMapOperation(val editor: MapEditor, val mapId: String, val callback: (MargoMap?) -> Unit) : SimpleOperation<LoadMapOperation>()
{
    override val name: String = "Ładowanie mapy: $mapId"

    override fun start0(operationCallback: OperationCallback<LoadMapOperation>)
    {
        operationCallback.operationProgress(this, 0, -1)

        val bundle = this.editor.editor.bundle
        val deserializer = MapDeserializer(this.editor.loadTilesets())

        // load maps
        val resource = bundle.getResource(MargoResource.Category.MAPS, this.mapId)

        if (resource == null)
        {
            this.callback(null)
            return
        }

        val sizePrimitive = resource.meta?.get("size") as? JsonPrimitive
        val size = if (sizePrimitive?.isNumber == true) sizePrimitive.asNumber.toInt() else -1

        operationCallback.operationProgress(this, 0, size)

        val stream = ProgressReportingInputStream(bundle.loadResource(resource)!!) { progress ->
            operationCallback.operationProgress(this, progress.toInt(), size)
        }

        val bytes = IOUtils.toByteArray(stream)
        val map = deserializer.deserialize(bytes)

        operationCallback.operationProgress(this, 0, -1)

        val npcsToLoad = ArrayList<ResourceView>()

        for (mapObject in map.objects)
        {
            if (mapObject !is NpcMapObject)
            {
                continue
            }

            val npcResource = bundle.getResource(MargoResource.Category.NPC, mapObject.id!!) ?: continue

            npcsToLoad.add(npcResource)
        }

        val graphicsToLoad = HashMap<ResourceView, String>(npcsToLoad.size)
        val npcDeserializer = NpcDeserializer()
        for ((npcView, npcStream) in bundle.loadAll(npcsToLoad))
        {
            npcStream ?: continue

            val npcIcon = npcDeserializer.deserialize(npcStream).graphics

            if (npcIcon.isEmpty())
            {
                continue
            }

            val graphics = bundle.getResource(MargoResource.Category.GRAPHIC, this.editor.editor.iconPathToIconId(GraphicResource.GraphicCategory.NPC, npcIcon)) ?: continue
            graphicsToLoad.put(graphics, npcView.id)
        }

        val graphicsDeserializer = GraphicDeserializer()
        for ((graphicsView, graphicsStream) in bundle.loadAll(graphicsToLoad.keys))
        {
            graphicsStream ?: continue

            val image = ImageIO.read(ByteArrayInputStream(graphicsDeserializer.deserialize(graphicsStream).icon.image))
            val npcImage = NpcImage(image)

            this.editor.npcCache.put(graphicsToLoad[graphicsView]!!, npcImage)
        }

        this.callback(map)
    }
}