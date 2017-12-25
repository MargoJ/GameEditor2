package pl.margoj.editor2.app.controller.dialog.tool

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.scene.control.*
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.gateway.GatewayObject
import java.net.URL
import java.util.*

class GatewayToolDialogController : AbstractToolDialogController<GatewayObject>()
{
    @FXML
    lateinit var fieldGatewayTargetName: TextField

    @FXML
    lateinit var buttonSelectMap: Button

    @FXML
    lateinit var fieldGatewayTargetX: TextField

    @FXML
    lateinit var fieldGatewayTargetY: TextField

    @FXML
    lateinit var buttonSelectPosition: Button

    @FXML
    lateinit var toggleKeyNeeded: ToggleButton

    @FXML
    lateinit var fieldGatewayKeyName: TextField

    @FXML
    lateinit var buttonSelectKey: Button

    @FXML
    lateinit var toggleLevelRestriction: ToggleButton

    @FXML
    lateinit var fieldGatewayLevelMin: TextField

    @FXML
    lateinit var fieldGatewayLevelMax: TextField

    @FXML
    lateinit var buttonGatewayConfirm: Button

    override fun loadData(data: Any)
    {
        super.loadData(data)

        val mapObject = this.mapObject

        if (mapObject != null)
        {
            this.fieldGatewayTargetName.text = mapObject.targetMap
            this.fieldGatewayTargetX.text = mapObject.target.x.toString()
            this.fieldGatewayTargetY.text = mapObject.target.y.toString()

            this.toggleKeyNeeded.isSelected = mapObject.keyId != null
            this.fieldGatewayKeyName.text = mapObject.keyId ?: ""

            this.toggleLevelRestriction.isSelected = mapObject.levelRestriction.enabled
            this.fieldGatewayLevelMin.text = mapObject.levelRestriction.minLevel.toString()
            this.fieldGatewayLevelMax.text = mapObject.levelRestriction.maxLevel.toString()
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        FXUtils.makeNumberField(this.fieldGatewayTargetX, false)
        FXUtils.makeNumberField(this.fieldGatewayTargetY, false)
        FXUtils.makeNumberField(this.fieldGatewayLevelMin, false, true)
        FXUtils.makeNumberField(this.fieldGatewayLevelMax, false, true)

        this.fieldGatewayKeyName.disableProperty().bind(this.toggleKeyNeeded.selectedProperty().not())
        this.toggleKeyNeeded.textProperty().bind(Bindings.`when`(this.toggleKeyNeeded.selectedProperty()).then("Tak").otherwise("Nie"))
        this.buttonSelectKey.disableProperty().bind(this.toggleKeyNeeded.selectedProperty().not())

        this.fieldGatewayLevelMin.disableProperty().bind(this.toggleLevelRestriction.selectedProperty().not())
        this.fieldGatewayLevelMax.disableProperty().bind(this.toggleLevelRestriction.selectedProperty().not())

        this.buttonSelectMap.setOnAction {
            this.editor.gui.showMapSelectDialog("Wybierz mape") { map ->
                this.fieldGatewayTargetName.text = map.id
            }
        }

        this.buttonSelectKey.setOnAction {
            val choices = this.editor.bundle.getResourcesByCategory(MargoResource.Category.ITEMS)
                    .filter { (ItemCategory.get(it.meta?.get("cat")?.asInt ?: ItemCategory.KEYS.margoId)) == ItemCategory.KEYS }
                    .map { "${it.name} [${it.id}]" to it }
                    .sortedBy { it.first }
                    .toMap()

            this.editor.gui.showSelectDialog("Wybierz klucz", choices) { key ->
                this.fieldGatewayKeyName.text = key.id
            }
        }

        this.buttonSelectPosition.setOnAction {
            this.mapEditor.loadMap(this.fieldGatewayTargetName.text) { map ->
                Platform.runLater {
                    if (map == null)
                    {
                        QuickAlert.create().error().header("Mapa nieznaleziona").content("Mapa o id ${this.fieldGatewayTargetName.text} nie została znaleziona!").showAndWait()
                        return@runLater
                    }

                    this.mapEditor.openSelect(map, "Wybierz miejsce na przejście") { point ->
                        Platform.runLater {
                            this.fieldGatewayTargetX.text = point.x.toString()
                            this.fieldGatewayTargetY.text = point.y.toString()
                        }
                    }
                }
            }
        }

        this.buttonGatewayConfirm.setOnAction {
            val errors = ArrayList<String>()
            val id = this.fieldGatewayTargetName.text
            val x = this.fieldGatewayTargetX.text.toInt()
            val y = this.fieldGatewayTargetY.text.toInt()

            if (!MargoResource.ID_PATTERN.matcher(id).matches())
            {
                errors.add("ID mapy nie jest poprawne")
            }

            if (x > 127 || y > 127)
            {
                errors.add("Koordynaty X i Y nie mogą przekraczać 127")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas edytowania przejścia", errors)
                return@setOnAction
            }

            val newGateway = GatewayObject(
                    position = this.position,
                    target = Point(x, y),
                    targetMap = id,
                    levelRestriction = GatewayObject.LevelRestriction(
                            enabled = this.toggleLevelRestriction.isSelected,
                            minLevel = this.fieldGatewayLevelMin.text.toIntOrNull() ?: 0,
                            maxLevel = this.fieldGatewayLevelMax.text.toIntOrNull() ?: 0
                    ), keyId = if (this.toggleKeyNeeded.isSelected) this.fieldGatewayKeyName.text else null
            )

            if (newGateway == this.mapObject)
            {
                this.scene.stage.close()
                return@setOnAction
            }

            this.mapEditor.setObject(this.position, newGateway)

            if (this.mapObject != null && (this.mapObject!!.target != newGateway.target || this.mapObject!!.targetMap != newGateway.targetMap))
            {
                val oldResponse = QuickAlert.create()
                        .confirmation()
                        .header("Usuąnąć przejście z drugej strony?")
                        .content("Czy usunąć przejście po drugiej stronie jeśli istnieje?")
                        .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO))
                        .showAndWait()

                if (oldResponse?.buttonData == ButtonBar.ButtonData.YES)
                {
                    this.editor.bundle.bundleOperation.deleteMatchingGateway(this.mapObject!!)
                }
            }

            val response = QuickAlert.create()
                    .confirmation()
                    .header("Utworzyć przejście na drugiej stronie?")
                    .content("Czy utworzyć przejście na drugiej mapie prowadzące do tej mapy?")
                    .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO))
                    .showAndWait()

            if (response?.buttonData == ButtonBar.ButtonData.YES)
            {
                this.editor.bundle.bundleOperation.createMatchingGateway(this.mapEditor.currentMap!!.id, newGateway)
            }

            this.scene.stage.close()
        }
    }
}