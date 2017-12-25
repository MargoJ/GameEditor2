package pl.margoj.editor2.app.controller.parts

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.MargoJEditor
import java.net.URL
import java.util.*

class TeleportController : CustomController
{
    lateinit var editor: MargoJEditor

    @FXML
    lateinit var fieldMap: TextField

    @FXML
    lateinit var buttonToggle: ToggleButton

    @FXML
    lateinit var buttonSelectMap: Button

    @FXML
    lateinit var fieldY: TextField

    @FXML
    lateinit var fieldX: TextField

    @FXML
    lateinit var buttonSelectPosition: Button

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        FXUtils.makeNumberField(this.fieldX, false)
        FXUtils.makeNumberField(this.fieldY, false)

        this.buttonSelectPosition.disableProperty().bind(this.buttonToggle.selectedProperty().not())
        this.fieldX.disableProperty().bind(this.buttonToggle.selectedProperty().not())
        this.fieldY.disableProperty().bind(this.buttonToggle.selectedProperty().not())

        this.buttonToggle.textProperty().bind(Bindings.`when`(this.buttonToggle.selectedProperty()).then("Własna pozycja").otherwise("Miejsce spawnu"))

        this.buttonSelectMap.setOnAction {
            this.editor.gui.showMapSelectDialog("Wybierz mape") { map ->
                this.fieldMap.text = map.id
            }
        }
        
        this.buttonSelectPosition.setOnAction {
            this.editor.mapEditor.loadMap(this.fieldMap.text) { map ->
                Platform.runLater {
                    if (map == null)
                    {
                        QuickAlert.create().error().header("Mapa nieznaleziona").content("Mapa o id ${this.fieldMap.text} nie została znaleziona!").showAndWait()
                        return@runLater
                    }

                    this.editor.mapEditor.openSelect(map, "Wybierz miejsce na przejście") { point ->
                        Platform.runLater {
                            this.fieldX.text = point.x.toString()
                            this.fieldY.text = point.y.toString()
                        }
                    }
                }
            }
        }

    }
}