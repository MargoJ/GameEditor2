package pl.margoj.editor2.app.scene

import javafx.scene.Scene
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.app.controller.StartController

class StartScene : CustomScene<StartController>("start")
{
    private val logger = LogManager.getLogger(this::javaClass)

    override fun setup(stage: Stage, scene: Scene, controller: StartController)
    {
        logger.trace("setup(stage = $stage, scene = $scene, controller = $controller)")

        this.setIcon("icon.png")

        stage.title = "MargoJ Edytor v2"

        stage.isResizable = false
        stage.sizeToScene()
    }
}