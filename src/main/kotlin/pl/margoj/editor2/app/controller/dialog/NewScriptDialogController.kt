package pl.margoj.editor2.app.controller.dialog

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.script.NpcScript
import java.net.URL
import java.util.*

class NewScriptDialogController : AbstractDialogController()
{
    @FXML
    lateinit var fieldScriptId: TextField

    @FXML
    lateinit var buttonScriptConfirm: Button

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        buttonScriptConfirm.setOnAction {
            val errors = ArrayList<String>()

            if (this.fieldScriptId.text.isEmpty())
            {
                errors.add("ID skryptu nie moze byc puste")
            }

            if (!MargoResource.ID_PATTERN.matcher(this.fieldScriptId.text).matches())
            {
                errors.add("ID może zawierać tylko znaki alfanumeryczne i _")
            }

            if (this.fieldScriptId.text.length > 127)
            {
                errors.add("ID skryptu nie moze przekraczac 127 znakow")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas tworzenia nowego skryptu", errors)
                return@setOnAction
            }

            this.editor.scriptEditor.edit(NpcScript(this.fieldScriptId.text))

            scene.stage.close()
        }
    }
}