package pl.margoj.editor2.app.controller.parts

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import pl.margoj.utils.javafx.api.CustomController
import java.net.URL
import java.util.ResourceBundle

class ItemPropertyController : CustomController
{
    @FXML
    lateinit var propLabelName: Label

    @FXML
    lateinit var propPaneValueHolder: StackPane

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
    }
}