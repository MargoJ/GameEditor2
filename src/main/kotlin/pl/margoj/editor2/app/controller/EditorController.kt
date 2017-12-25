package pl.margoj.editor2.app.controller

import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.gui.EditorGUI
import pl.margoj.editor2.editor.gui.map.canvasevents.EditorCanvasMouseEventManager
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import java.net.URL
import java.util.*

class EditorController : CustomController
{
    lateinit var editor: MargoJEditor

    lateinit var scene: CustomScene<*>

    @FXML
    lateinit var indexList: TreeView<EditorGUI.CustomText>

    @FXML
    lateinit var indexRefresh: Button

    @FXML
    lateinit var labelMapName: Label

    @FXML
    lateinit var labelCursorInfo: Label

    @FXML
    lateinit var toggleLayer1: RadioButton

    @FXML
    lateinit var toggleLayer2: RadioButton

    @FXML
    lateinit var toggleLayer3: RadioButton

    @FXML
    lateinit var toggleLayer4: RadioButton

    @FXML
    lateinit var toggleLayer5: RadioButton

    @FXML
    lateinit var toggleLayer6: RadioButton

    @FXML
    lateinit var toggleLayer7: RadioButton

    @FXML
    lateinit var toggleLayer8: RadioButton

    @FXML
    lateinit var toggleLayer9: RadioButton

    @FXML
    lateinit var toggleLayerP: RadioButton

    @FXML
    lateinit var toggleLayerC: RadioButton

    @FXML
    lateinit var toggleLayerW: RadioButton

    @FXML
    lateinit var mapCanvasHolder: AnchorPane

    @FXML
    lateinit var mapScrollHorizontal: ScrollBar

    @FXML
    lateinit var mapScrollVertical: ScrollBar

    @FXML
    lateinit var buttonScrollZero: Button

    @FXML
    lateinit var buttonSelectTileset: Button

    @FXML
    lateinit var tilesetCanvas: Canvas

    @FXML
    lateinit var tilesetCanvasScroll: ScrollPane

    @FXML
    lateinit var checkboxShowGrid: CheckBox

    @FXML
    lateinit var checkboxDrawCurrentLayer: CheckBox

    @FXML
    lateinit var save: Button

    @FXML
    lateinit var undo: Button

    @FXML
    lateinit var redo: Button

    @FXML
    lateinit var edit: Button

    @FXML
    lateinit var meta: Button

    @FXML
    lateinit var preview: Button

    @FXML
    lateinit var indexTabPane: TabPane

    @FXML
    lateinit var toolSelect: RadioButton

    @FXML
    lateinit var toolPaint: RadioButton

    @FXML
    lateinit var toolEraser: RadioButton

    @FXML
    lateinit var toolFill: RadioButton

    @FXML
    lateinit var toolObject: RadioButton

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene

        scene.stage.setOnCloseRequest { event ->
            if (!this.editor.mapEditor.askForSaveIfNecessary() || !this.editor.askForSaveIfNecessary())
            {
                event.consume()
            }
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {

    }

    override fun loadData(data: Any)
    {
        this.editor = data as MargoJEditor

        this.editor.gui.init(this.scene, this)

        this.editor.mapEditor.renderer.init(this.mapCanvasHolder, this.mapScrollHorizontal, this.mapScrollVertical, this.buttonScrollZero)

        this.editor.gui.layerBar.init(
                this.toggleLayer1, this.toggleLayer2, this.toggleLayer3, this.toggleLayer4, this.toggleLayer5, this.toggleLayer6,
                this.toggleLayer7, this.toggleLayer8, this.toggleLayer9, this.toggleLayerP, this.toggleLayerC, this.toggleLayerW
        )

        this.editor.gui.toolsBar.init(
                this.toolSelect, this.toolPaint, this.toolEraser, this.toolFill, this.toolObject
        )

        EditorCanvasMouseEventManager(this.editor.mapEditor).init(this.editor.mapEditor.renderer)
    }
}