package pl.margoj.editor2.app.scene

import javafx.scene.Scene
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.app.controller.ItemController
import pl.margoj.editor2.editor.item.ItemEditor
import pl.margoj.mrf.item.MargoItem

class ItemScene(val editor: ItemEditor, val item: MargoItem) : CustomScene<ItemController>("item", Pair(editor, item))
{
    private val logger = LogManager.getLogger(this::class.java)

    override fun setup(stage: Stage, scene: Scene, controller: ItemController)
    {
        logger.trace("setup(stage = $stage, scene = $scene, controller = $controller)")

        this.setIcon("icon.png")

        stage.title = "Przedmiot: ${this.item.id}"

        stage.isResizable = true
        stage.sizeToScene()
    }
}