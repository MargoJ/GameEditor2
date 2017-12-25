package pl.margoj.editor2.app.controller.dialog

import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.undo.MetadataUndoRedo
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.metadata.ismain.IsMain
import pl.margoj.mrf.map.metadata.istown.IsTown
import pl.margoj.mrf.map.metadata.parentmap.ParentMap
import pl.margoj.mrf.map.metadata.pvp.MapPvP
import pl.margoj.mrf.map.metadata.respawnmap.RespawnMap
import pl.margoj.mrf.map.metadata.welcome.WelcomeMessage
import java.net.URL
import java.util.*

class MapMetadataDialogController : CustomController
{
    private val options: Map<MapPvP, String> = hashMapOf(
            Pair(MapPvP.NO_PVP, "PvP wyłączone"),
            Pair(MapPvP.CONDITIONAL, "PvP za zgodą"),
            Pair(MapPvP.UNCONDITIONAL, "PvP bezwarunkowe"),
            Pair(MapPvP.ARENAS, "Areny")
    )

    lateinit var editor: MapEditor

    lateinit var scene: CustomScene<*>

    lateinit var map: MargoMap

    @FXML
    lateinit var fieldWelcomeMessage: TextField

    @FXML
    lateinit var fieldParentMap: TextField

    @FXML
    lateinit var buttonSelectParent: Button

    @FXML
    lateinit var fieldRespawnMap: TextField

    @FXML
    lateinit var buttonSelectRespawn: Button

    @FXML
    lateinit var choicePvP: ChoiceBox<String>

    @FXML
    lateinit var toggleMain: ToggleButton

    @FXML
    lateinit var toggleTown: ToggleButton

    @FXML
    lateinit var buttonConfirm: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun loadData(data: Any)
    {
        @Suppress("UNCHECKED_CAST")
        data as Pair<MapEditor, MargoMap>

        this.editor = data.first
        this.map = data.second

        this.choicePvP.items.setAll(this.options.values)
        this.choicePvP.selectionModel.select(this.options[this.map.getMetadata(MapPvP::class.java)])

        this.fieldWelcomeMessage.text = this.map.getMetadata(WelcomeMessage::class.java).value
        this.fieldParentMap.text = this.map.getMetadata(ParentMap::class.java).value
        this.fieldRespawnMap.text = this.map.getMetadata(RespawnMap::class.java).value

        this.toggleMain.isSelected = this.map.getMetadata(IsMain::class.java).value
        this.toggleTown.isSelected = this.map.getMetadata(IsTown::class.java).value
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        this.toggleMain.textProperty().bind(Bindings.`when`(this.toggleMain.selectedProperty()).then("Główna").otherwise("Poboczna"))
        this.toggleTown.textProperty().bind(Bindings.`when`(this.toggleTown.selectedProperty()).then("Tak").otherwise("Nie"))

        this.fieldParentMap.disableProperty().bind(Bindings.`when`(this.toggleMain.selectedProperty()).then(true).otherwise(false))
        this.buttonSelectParent.disableProperty().bind(Bindings.`when`(this.toggleMain.selectedProperty()).then(true).otherwise(false))

        this.buttonSelectParent.setOnAction {
            this.editor.gui.editorGUI.showMapSelectDialog("Wybierz mapę nadrzędną") {
                this.fieldParentMap.text = it.id
            }
        }

        this.buttonSelectRespawn.setOnAction {
            this.editor.gui.editorGUI.showMapSelectDialog("Wybierz mapę respawnu") {
                this.fieldRespawnMap.text = it.id
            }
        }

        this.buttonConfirm.onAction = EventHandler {
            var anyChanges = false

            val oldMeta = this.map.metadata

            val newPvP = this.textToOption(this.choicePvP.selectionModel.selectedItem)
            if (map.getMetadata(MapPvP::class.java) != newPvP)
            {
                map.setMetadata(newPvP)
                anyChanges = true
            }

            val newWelcome = this.fieldWelcomeMessage.text
            if (map.getMetadata(WelcomeMessage::class.java).value != newWelcome)
            {
                map.setMetadata(WelcomeMessage(newWelcome))
                anyChanges = true
            }

            val newParentMap = this.fieldParentMap.text
            if (map.getMetadata(ParentMap::class.java).value != newParentMap)
            {
                map.setMetadata(ParentMap(newParentMap))
                anyChanges = true
            }

            val newRespawnMap = this.fieldRespawnMap.text
            if (map.getMetadata(RespawnMap::class.java).value != newRespawnMap)
            {
                map.setMetadata(RespawnMap(newRespawnMap))
                anyChanges = true
            }

            val newIsMain = this.toggleMain.isSelected
            if (map.getMetadata(IsMain::class.java).value != newIsMain)
            {
                map.setMetadata(IsMain(newIsMain))
                anyChanges = true
            }


            val newIsTown = this.toggleTown.isSelected
            if (map.getMetadata(IsTown::class.java).value != newIsTown)
            {
                map.setMetadata(IsTown(newIsTown))
                anyChanges = true
            }

            if (anyChanges)
            {
                this.editor.addUndoAction(MetadataUndoRedo(this.editor, oldMeta, map.metadata))
            }

            this.scene.stage.close()
        }
    }

    private fun textToOption(option: String): MapPvP
    {
        return this.options.entries.stream().filter { it.value == option }.map { it.key }.findAny().get()
    }
}