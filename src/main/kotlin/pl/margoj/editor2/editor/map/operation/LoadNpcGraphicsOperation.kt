package pl.margoj.editor2.editor.map.operation

import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.map.utils.NpcImage
import pl.margoj.editor2.editor.operation.SimpleOperation
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.graphics.GraphicDeserializer
import pl.margoj.mrf.graphics.GraphicResource
import pl.margoj.mrf.npc.NpcDeserializer
import pl.margoj.utils.javafx.operation.OperationCallback
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class LoadNpcGraphicsOperation(val editor: MargoJEditor, val npcId: String, val callback: () -> Unit) : SimpleOperation<LoadNpcGraphicsOperation>()
{
    override val name: String = "Ładowanie grafiki dla NPC"

    override fun start0(operationCallback: OperationCallback<LoadNpcGraphicsOperation>)
    {
        operationCallback.operationProgress(this, 0, -1)

        val npcView = this.editor.bundle.getResource(MargoResource.Category.NPC, npcId)
        val npcStream = if (npcView == null) null else this.editor.bundle.loadResource(npcView)

        if (npcStream == null)
        {
            callback()
            return
        }

        val npcGraphic = NpcDeserializer().deserialize(npcStream).graphics
        val graphicsView = if(npcGraphic.isEmpty()) null else this.editor.bundle.getResource(MargoResource.Category.GRAPHIC, this.editor.iconPathToIconId(GraphicResource.GraphicCategory.NPC, npcGraphic))
        val graphicsStream = if(graphicsView == null) null else this.editor.bundle.loadResource(graphicsView)

        if (graphicsStream == null)
        {
            callback()
            return
        }

        val image = NpcImage(ImageIO.read(ByteArrayInputStream(GraphicDeserializer().deserialize(graphicsStream).icon.image)))

        this.editor.mapEditor.npcCache.put(this.npcId, image)

        callback()
    }
}