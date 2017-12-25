package pl.margoj.editor2.app.scene

import javafx.scene.Scene
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.app.controller.GraphicsController
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.graphics.GraphicResource

class GraphicsScene(val editor: MargoJEditor, val graphicsCategory: GraphicResource.GraphicCategory, callback: (GraphicResource) -> Unit) : CustomScene<GraphicsController>("graphics", Triple(editor, graphicsCategory, callback))
{
    private val logger = LogManager.getLogger(this::class.java)

    override fun setup(stage: Stage, scene: Scene, controller: GraphicsController)
    {
        logger.trace("setup(stage = $stage, scene = $scene, controller = $controller)")

        this.setIcon("icon.png")

        stage.title = "Przeglądanie grafik z folderu: ${this.graphicsCategory.displayName}"

        stage.isResizable = true
        stage.sizeToScene()
    }
}