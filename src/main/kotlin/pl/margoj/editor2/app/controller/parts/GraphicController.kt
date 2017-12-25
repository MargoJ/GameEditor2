package pl.margoj.editor2.app.controller.parts

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import pl.margoj.utils.javafx.api.CustomController
import java.net.URL
import java.util.*

class GraphicController : CustomController
{
    @FXML
    lateinit var container: VBox

    @FXML
    lateinit var labelName: Label

    @FXML
    lateinit var imageView: ImageView

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
    }
}