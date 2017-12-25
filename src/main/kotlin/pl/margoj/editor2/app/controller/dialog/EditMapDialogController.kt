package pl.margoj.editor2.app.controller.dialog

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.TextField
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.map.undo.MapNameChange
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.serialization.MapSerializer
import java.net.URL
import java.util.*

class EditMapDialogController : AbstractDialogController()
{
    var map: MargoMap? = null

    @FXML
    lateinit var fieldMapId: TextField

    @FXML
    lateinit var fieldMapName: TextField

    @FXML
    lateinit var fieldMapWidth: TextField

    @FXML
    lateinit var fieldMapHeight: TextField

    @FXML
    lateinit var buttonMapConfirm: Button

    @Suppress("UNCHECKED_CAST")
    override fun loadData(data: Any)
    {
        if (data is MargoJEditor)
        {
            super.loadData(data)
        }
        else
        {
            val (editor, map) = data as Pair<MargoJEditor, MargoMap>
            super.loadData(editor)
            this.map = map
            this.fieldMapId.isDisable = true
            this.fieldMapId.text = map.id
            this.fieldMapName.text = map.name
            this.fieldMapWidth.text = map.width.toString()
            this.fieldMapHeight.text = map.height.toString()
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        FXUtils.makeNumberField(this.fieldMapWidth, false)
        FXUtils.makeNumberField(this.fieldMapHeight, false)

        buttonMapConfirm.setOnAction {
            val errors = ArrayList<String>()

            var width: Int
            var height: Int

            try
            {
                width = Integer.parseInt(this.fieldMapWidth.text)
                height = Integer.parseInt(this.fieldMapHeight.text)
            }
            catch (e: NumberFormatException)
            {
                width = Integer.MAX_VALUE
                height = Integer.MAX_VALUE
            }

            if (width < 16 || height < 16)
            {
                errors.add("Wysokość i szerokosc nie moga byc mniejsze od 16")
            }

            if (width > 128 || height > 128)
            {
                errors.add("Wysokość i szerokość nie mogą przekraczać 128")
            }

            if (this.fieldMapName.text.length > 127)
            {
                errors.add("Nazwa mapy nie moze przekraczac 127 znakow")
            }

            if (this.fieldMapId.text.isEmpty())
            {
                errors.add("ID mapy nie moze byc puste")
            }

            if (!MargoResource.ID_PATTERN.matcher(this.fieldMapId.text).matches())
            {
                errors.add("ID może zawierać tylko znaki alfanumeryczne i _")
            }

            if (this.fieldMapId.text.length > 127)
            {
                errors.add("ID mapy nie moze przekraczac 127 znakow")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas ustawiania właściwości mapy", errors)
                return@setOnAction
            }

            val map = this.map

            if (map == null)
            {
                this.editor.mapEditor.currentMap = MargoMap(MapSerializer.CURRENT_VERSION.toByte(), fieldMapId.text, fieldMapName.text, width, height)
            }
            else
            {
                val newName = this.fieldMapName.text
                val oldName = map.name
                map.name = newName

                val newWidth = this.fieldMapWidth.text.toInt()
                val newHeight = this.fieldMapHeight.text.toInt()

                if(map.width != newWidth || map.height != newHeight)
                {
                    val result=  QuickAlert.create()
                            .confirmation()
                            .header("Czy na pewno chcesz zmienić rozmiar mapy?")
                            .content("Zmiana rozmiaru mapy spowoduje usunięcie historii zmian co uniemożliwi cofnięcie poprzednich zmian")
                            .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO))
                            .showAndWait()

                    if(result?.buttonData != ButtonBar.ButtonData.YES)
                    {
                        return@setOnAction
                    }

                    map.resize(newWidth, newHeight)
                    this.editor.mapEditor.clearUndoRedoHistory()
                }

                this.editor.mapEditor.currentMap = map

                if(newName != oldName)
                {
                    this.editor.mapEditor.addUndoAction(MapNameChange(this.editor.mapEditor, oldName, newName))
                }
            }

            scene.stage.close()
        }
    }
}