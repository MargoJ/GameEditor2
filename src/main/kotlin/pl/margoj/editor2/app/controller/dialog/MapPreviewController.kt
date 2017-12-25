package pl.margoj.editor2.app.controller.dialog

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.RadioButton
import javafx.scene.control.ScrollBar
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.AnchorPane
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.gui.map.canvasevents.SelectCanvasMouseEventManager
import pl.margoj.editor2.editor.gui.map.canvasevents.SimpleCanvasMouseEventManager
import pl.margoj.editor2.editor.map.render.PreviewMapRenderer
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import java.net.URL
import java.util.*

class MapPreviewController : AbstractDialogController()
{
    lateinit var map: MargoMap

    lateinit var renderer: PreviewMapRenderer

    var callback: ((Point) -> Unit)? = null

    @FXML
    lateinit var zoom25: RadioButton

    @FXML
    lateinit var zoom50: RadioButton

    @FXML
    lateinit var zoom100: RadioButton

    @FXML
    lateinit var canvasHolder: AnchorPane

    @FXML
    lateinit var scrollVertical: ScrollBar

    @FXML
    lateinit var scrollHorizontal: ScrollBar

    @FXML
    lateinit var buttonCenter: Button

    @Suppress("UNCHECKED_CAST")
    override fun loadData(data: Any)
    {
        data as Triple<MargoJEditor, MargoMap, ((Point) -> Unit)?>
        super.loadData(data.first)
        this.map = data.second
        this.callback = data.third

        this.renderer = PreviewMapRenderer(this.editor, this.map)
        this.renderer.init(this.canvasHolder, this.scrollHorizontal, this.scrollVertical, this.buttonCenter)

        if (this.callback != null)
        {
            SelectCanvasMouseEventManager(this.scene.stage, this.renderer, this.callback!!).init(this.renderer)
        }
        else
        {
            SimpleCanvasMouseEventManager().init(this.renderer)
        }

        this.updatePointSize(32)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        val buttonGroup = ToggleGroup()
        this.zoom25.toggleGroup = buttonGroup
        this.zoom50.toggleGroup = buttonGroup
        this.zoom100.toggleGroup = buttonGroup
        this.zoom100.isSelected = true

        this.zoom25.setOnAction { this.updatePointSize(8) }
        this.zoom50.setOnAction { this.updatePointSize(16) }
        this.zoom100.setOnAction { this.updatePointSize(32) }
    }

    fun updatePointSize(pointSize: Int)
    {
        this.renderer.pointSize = pointSize
        this.renderer.resetWholeCache()
        this.renderer.resizeIfNeeded()
    }
}