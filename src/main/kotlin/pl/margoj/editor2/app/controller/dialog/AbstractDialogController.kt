package pl.margoj.editor2.app.controller.dialog

import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.editor.MargoJEditor

abstract class AbstractDialogController : CustomController
{
    lateinit var editor: MargoJEditor
    lateinit var scene: CustomScene<*>

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun loadData(data: Any)
    {
        this.editor = data as MargoJEditor
    }
}