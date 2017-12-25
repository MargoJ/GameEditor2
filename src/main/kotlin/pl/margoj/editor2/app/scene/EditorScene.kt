package pl.margoj.editor2.app.scene

import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.app.controller.EditorController
import pl.margoj.editor2.editor.MargoJEditor

class EditorScene(val editor: MargoJEditor) : CustomScene<EditorController>("editor", editor)
{
    private val logger = LogManager.getLogger(this::javaClass)

    override fun setup(stage: Stage, scene: Scene, controller: EditorController)
    {
        logger.trace("setup(stage = $stage, scene = $scene, controller = $controller)")

        this.setIcon("icon.png")

        stage.title = "MargoJ Edytor v2"

        val pane = scene.root as Pane
        stage.minWidth = pane.minWidth
        stage.minHeight = pane.minHeight

        stage.isResizable = true
        stage.sizeToScene()
    }
}