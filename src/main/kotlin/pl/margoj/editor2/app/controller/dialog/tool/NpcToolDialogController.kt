package pl.margoj.editor2.app.controller.dialog.tool

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import pl.margoj.editor2.editor.map.operation.LoadNpcGraphicsOperation
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.ResourceView
import pl.margoj.mrf.map.objects.npc.NpcMapObject
import pl.margoj.utils.javafx.utils.FXUtils
import java.net.URL
import java.util.*

class NpcToolDialogController : AbstractToolDialogController<NpcMapObject>()
{
    @FXML
    lateinit var fieldNpc: TextField

    @FXML
    lateinit var buttonSelectNpc: Button

    @FXML
    lateinit var fieldGroup: TextField

    @FXML
    lateinit var buttonGroupPlus: Button

    @FXML
    lateinit var buttonGroupMinus: Button

    @FXML
    lateinit var buttonNpcConfirm: Button

    private companion object
    {
        var lastNpc = ""
        var lastGroup = "0"
    }

    override fun loadData(data: Any)
    {
        super.loadData(data)

        val mapObject = this.mapObject

        this.fieldNpc.text = mapObject?.id ?: lastNpc
        this.fieldGroup.text = mapObject?.group?.toString() ?: lastGroup
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        FXUtils.makeNumberField(this.fieldGroup, false)

        this.buttonSelectNpc.setOnAction {
            val choices: Map<String, ResourceView> = this.editor.bundle
                    .getResourcesByCategory(MargoResource.Category.NPC)
                    .map { "${it.name} [${it.id}]" to it }
                    .toMap()

            this.editor.gui.showSelectDialog("Wybierz npc", choices) { npc ->
                this.fieldNpc.text = npc.id
            }
        }

        this.buttonGroupPlus.setOnAction {
            this.fieldGroup.text = Math.min(Byte.MAX_VALUE.toInt(), ((this.fieldGroup.text.toIntOrNull() ?: 0) + 1)).toString()
        }

        this.buttonGroupMinus.setOnAction {
            this.fieldGroup.text = Math.max(0, ((this.fieldGroup.text.toIntOrNull() ?: 0) - 1)).toString()
        }

        this.buttonNpcConfirm.setOnAction {
            val errors = ArrayList<String>()

            val group = this.fieldGroup.text.toInt()

            if (group > Byte.MAX_VALUE)
            {
                errors.add("Grupa nie może przekraczać ${Byte.MAX_VALUE}")
            }

            if (errors.isNotEmpty())
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił błąd podczas ustawiania NPC", errors)
                return@setOnAction
            }

            val newNpc = NpcMapObject(this.position, this.fieldNpc.text, group.toByte())

            lastNpc = this.fieldNpc.text
            lastGroup = this.fieldGroup.text

            if (newNpc == this.mapObject)
            {
                this.scene.stage.close()
                return@setOnAction
            }

            val loadNpc = !this.mapEditor.npcCache.containsKey(this.fieldNpc.text)

            val callback = {
                Platform.runLater {
                    this.mapEditor.setObject(this.position, newNpc)
                    this.scene.stage.close()
                }
            }

            if (loadNpc)
            {
                this.editor.startOperation(LoadNpcGraphicsOperation(this.editor, this.fieldNpc.text, callback))
            }
            else
            {
                callback()
            }
        }
    }
}