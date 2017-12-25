package pl.margoj.editor2.app.scene

import javafx.scene.Scene
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.app.controller.ScriptController
import pl.margoj.editor2.editor.script.ScriptEditor
import pl.margoj.mrf.script.NpcScript

class ScriptScene(val editor: ScriptEditor, val script: NpcScript) : CustomScene<ScriptController>("script", Pair(editor, script))
{
    private val logger = LogManager.getLogger(this::class.java)

    override fun setup(stage: Stage, scene: Scene, controller: ScriptController)
    {
        logger.trace("setup(stage = $stage, scene = $scene, controller = $controller)")

        this.setIcon("icon.png")

        stage.title = "Skrypt: ${this.script.id}"

        stage.isResizable = true
        stage.sizeToScene()
    }
}