package pl.margoj.editor2.editor.operation

import javafx.application.Platform
import pl.margoj.editor2.EditorApplication
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.utils.javafx.operation.OperationCallback
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.data.DataConstants
import pl.margoj.mrf.data.DataDeserializer
import pl.margoj.mrf.data.DataResource
import pl.margoj.mrf.data.GameData

class LoadGameSettingsOperation(val editor: MargoJEditor) : SimpleOperation<LoadGameSettingsOperation>()
{
    override val name: String = "Ładowanie ustawień gry"

    override fun start0(operationCallback: OperationCallback<LoadGameSettingsOperation>)
    {
        val gameData: DataResource<GameData>

        val gameDataResource = this.editor.bundle.getResource(MargoResource.Category.DATA, DataConstants.GAME_DATA)

        if (gameDataResource == null)
        {
            gameData = DataResource(DataConstants.GAME_DATA)
            gameData.content = GameData()
        }
        else
        {
            val stream = this.editor.bundle.loadResource(gameDataResource)!!
            val deserializer = DataDeserializer(DataConstants.GAME_DATA, GameData::class.java)
            gameData = deserializer.deserialize(stream)
        }

        Platform.runLater {
            FXUtils.loadDialog(EditorApplication::class.java.classLoader, "game", "Ustawienia gry", this.editor.gui.scene.stage, Pair(this.editor, gameData))
        }
    }
}