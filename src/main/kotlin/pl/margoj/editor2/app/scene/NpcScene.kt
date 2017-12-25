package pl.margoj.editor2.app.scene

import javafx.scene.Scene
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.app.controller.NpcController
import pl.margoj.editor2.editor.npc.NpcEditor
import pl.margoj.mrf.npc.MargoNpc

class NpcScene(val editor: NpcEditor, val npc: MargoNpc?) : CustomScene<NpcController>("npc", Pair(editor, npc))
{
    private val logger = LogManager.getLogger(this::class.java)

    override fun setup(stage: Stage, scene: Scene, controller: NpcController)
    {
        logger.trace("setup(stage = $stage, scene = $scene, controller = $controller)")

        this.setIcon("icon.png")

        stage.title = if (this.npc == null) "Dodaj nowego NPC" else "Npc: ${this.npc.id}"

        stage.isResizable = true
        stage.sizeToScene()
    }
}