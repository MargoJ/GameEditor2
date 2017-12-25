package pl.margoj.editor2.app.controller.dialog

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.item.MargoItem
import java.net.URL
import java.util.*

class NewItemDialogController : AbstractDialogController()
{
    @FXML
    lateinit var fieldItemId: TextField

    @FXML
    lateinit var buttonItemConfirm: Button

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        buttonItemConfirm.setOnAction {
            val errors = ArrayList<String>()

            if (this.fieldItemId.text.isEmpty())
            {
                errors.add("ID przedmiotu nie moze byc puste")
            }

            if (!MargoResource.ID_PATTERN.matcher(this.fieldItemId.text).matches())
            {
                errors.add("ID może zawierać tylko znaki alfanumeryczne i _")
            }

            if (this.fieldItemId.text.length > 127)
            {
                errors.add("ID przedmiotu nie moze przekraczac 127 znakow")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas tworzenia nowego przedmiotu", errors)
                return@setOnAction
            }

            this.editor.itemEditor.edit(MargoItem(this.fieldItemId.text, ""))

            scene.stage.close()
        }
    }
}