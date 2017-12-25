package pl.margoj.editor2.app.controller

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.stage.FileChooser
import pl.margoj.editor2.DEBUGGING_PROFILE
import pl.margoj.editor2.app.scene.EditorScene
import pl.margoj.editor2.app.scene.StartScene
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.utils.FileUtils
import pl.margoj.mrf.bundle.MargoResourceBundle
import pl.margoj.mrf.bundle.local.MargoMRFResourceBundle
import pl.margoj.mrf.bundle.local.MountedResourceBundle
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import java.io.File
import java.net.URL
import java.util.*

class StartController : CustomController
{
    lateinit var scene: StartScene

    @FXML
    lateinit var buttonResourceNew: Button

    @FXML
    lateinit var buttonResourceMrf: Button

    @FXML
    lateinit var buttonResourceRemote: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene as StartScene
    }

    override fun initialize(location: URL?, b: ResourceBundle?)
    {
        this.buttonResourceNew.setOnAction {
            this.init(MountedResourceBundle(File(FileUtils.MOUNT_DIRECTORY, System.currentTimeMillis().toString())))
        }

        this.buttonResourceMrf.setOnAction {
            val fileChooser = FileChooser()

            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Zestaw zasobow MargoJ (*.mrf)", "*.mrf"))
            fileChooser.title = "Wybierz plik"

            val file = fileChooser.showOpenDialog(scene.stage) ?: return@setOnAction

            val bundle = MargoMRFResourceBundle(file, File(FileUtils.MOUNT_DIRECTORY, System.currentTimeMillis().toString()))
            this.init(bundle)
        }
    }

    private fun init(bundle: MargoResourceBundle): MargoJEditor
    {
        val editor = MargoJEditor(DEBUGGING_PROFILE!!, bundle)
        val scene = EditorScene(editor)

        this.scene.loadAnother(scene)

        editor.init()

        return editor
    }
}