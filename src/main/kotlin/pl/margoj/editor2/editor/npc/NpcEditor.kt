package pl.margoj.editor2.editor.npc

import javafx.application.Platform
import javafx.stage.Modality
import javafx.stage.Stage
import pl.margoj.editor2.EditorApplication
import pl.margoj.editor2.app.scene.NpcScene
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.operation.LoadResourceOperation
import pl.margoj.mrf.ResourceView
import pl.margoj.mrf.npc.MargoNpc
import pl.margoj.mrf.npc.NpcDeserializer

class NpcEditor(val editor: MargoJEditor)
{
    fun requestNewNpc()
    {
        this.edit(null as MargoNpc?)
    }

    fun edit(view: ResourceView? = null)
    {
        if (view == null)
        {
            this.requestNewNpc()
            return
        }

        this.editor.startOperation(LoadResourceOperation(this.editor, view, NpcDeserializer(), { npc ->
            Platform.runLater {
                this.edit(npc!!)
            }
        }))
    }

    fun edit(npc: MargoNpc? = null)
    {
        val scene = NpcScene(this, npc)
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.initOwner(this.editor.gui.controller.scene.stage)
        scene.stage = stage
        scene.load(EditorApplication::class.java.classLoader)
        stage.show()
    }
}