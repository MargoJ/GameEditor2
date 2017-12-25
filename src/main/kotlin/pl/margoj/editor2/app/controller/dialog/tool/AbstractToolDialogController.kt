package pl.margoj.editor2.app.controller.dialog.tool

import pl.margoj.editor2.app.controller.dialog.AbstractDialogController
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.MapObject

abstract class AbstractToolDialogController<M : MapObject<M>> : AbstractDialogController()
{
    lateinit var mapEditor: MapEditor
    lateinit var map: MargoMap
    lateinit var position: Point
    var mapObject: M? = null

    @Suppress("UNCHECKED_CAST")
    override fun loadData(data: Any)
    {
        data as Data

        super.loadData(data.editor)
        this.mapEditor = data.editor.mapEditor
        this.map = data.map
        this.position = data.point
        this.mapObject = data.mapObject as? M
    }

    data class Data(val editor: MargoJEditor, val map: MargoMap, val point: Point, val mapObject: MapObject<*>?)
}