package pl.margoj.editor2.app.controller.dialog

import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.data.DataResource
import pl.margoj.mrf.data.DataSerializer
import pl.margoj.mrf.data.GameData
import pl.margoj.mrf.item.ItemCategory
import java.io.ByteArrayInputStream
import java.net.URL
import java.util.*
import kotlin.collections.LinkedHashMap

class GameSettingsDialogController : CustomController
{
    private lateinit var editor: MargoJEditor
    private lateinit var scene: CustomScene<*>

    @FXML
    lateinit var spawnHolder: GridPane

    @FXML
    lateinit var fieldBag: TextField

    @FXML
    lateinit var buttonSelectBag: Button

    @FXML
    lateinit var buttonSave: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene

        scene.stage.setOnCloseRequest {
            val result = QuickAlert.create()
                    .confirmation()
                    .header("Czy chcesz zapisać?")
                    .content("Czy chcesz zapisać ustawienia gry?")
                    .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO), ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE))
                    .showAndWait()

            when (result?.buttonData)
            {
                ButtonBar.ButtonData.YES ->
                {
                    this.buttonSave.fire()
                }
                ButtonBar.ButtonData.NO ->
                {
                }
                else -> it.consume()
            }
        }
    }

    override fun loadData(data: Any)
    {
        @Suppress("UNCHECKED_CAST")
        data as Pair<MargoJEditor, DataResource<GameData>>

        this.editor = data.first
        val gameData = data.second

        var index = 0
        val charToField = hashMapOf<Char, TextField>()

        for ((id, name) in SPAWN_DATA)
        {
            val label = Label(name)
            val field = TextField()
            val chooseButton = Button("...")

            chooseButton.setOnAction {
                this.editor.gui.showMapSelectDialog("Wybierz mapę spawnu: $name") { map ->
                    field.text = map.id
                }
            }

            field.text = gameData.content.spawns[id] ?: ""

            chooseButton.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
            GridPane.setMargin(field, Insets(0.0, 5.0, 0.0, 0.0))

            this.spawnHolder.addRow(index, label, field, chooseButton)
            GridPane.setColumnIndex(label, 0)
            GridPane.setColumnIndex(field, 1)
            GridPane.setColumnIndex(chooseButton, 2)
            index++

            charToField.put(id, field)
        }

        this.fieldBag.text = gameData.content.defaultBag

        this.buttonSelectBag.setOnAction {
            val choices = this.editor.bundle.getResourcesByCategory(MargoResource.Category.ITEMS)
                    .filter { (ItemCategory[it.meta?.get("cat")?.asInt ?: ItemCategory.BAGS.margoId]) == ItemCategory.BAGS }
                    .map { "${it.name} [${it.id}]" to it }
                    .sortedBy { it.first }
                    .toMap()

            this.editor.gui.showSelectDialog("Wybierz domyślną torbe", choices) { bag ->
                this.fieldBag.text = bag.id
            }
        }

        this.buttonSave.setOnAction {
            for ((char, field) in charToField)
            {
                gameData.content.spawns[char] = field.text
            }

            gameData.content.defaultBag = this.fieldBag.text

            this.editor.bundle.saveResource(gameData, ByteArrayInputStream(DataSerializer<GameData>().serialize(gameData)))
            QuickAlert.create().information().header("Zapisano!").content("Ustawienia gry zostały zapisane poprawnie!").showAndWait()
        }
    }

    private companion object
    {
        val SPAWN_DATA = LinkedHashMap<Char, String>()

        init
        {
            SPAWN_DATA.put('w', "Wojownik")
            SPAWN_DATA.put('p', "Paladyn")
            SPAWN_DATA.put('m', "Mag")
            SPAWN_DATA.put('t', "Tropiciel")
            SPAWN_DATA.put('h', "Łowca")
            SPAWN_DATA.put('b', "Tancerz Ostrzy")
            SPAWN_DATA.put('d', "Domyślny")
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
    }
}