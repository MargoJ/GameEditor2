package pl.margoj.editor2.editor.map.objects.tools

import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.objects.ObjectTool
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.gateway.GatewayObject
import java.awt.Graphics2D

class GatewayObjectTool : ObjectTool<GatewayObject>
{
    private val image = FXUtils.loadAwtImage("objects/gateway.png")

    override val mapObjectType: Class<GatewayObject> = GatewayObject::class.java

    override val name: String = "Przejście"

    override fun edit(editor: MapEditor, map: MargoMap, point: Point, mapObject: GatewayObject?)
    {
        editor.gui.openToolGui("gateway", map, point, mapObject)
    }

    override fun delete(editor: MapEditor, map: MargoMap, mapObject: GatewayObject)
    {
        val response = QuickAlert.create()
                .confirmation()
                .header("Usuąnąć przejście z drugej strony?")
                .content("Czy usunąć przejście po drugiej stronie jeśli istnieje?")
                .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO))
                .showAndWait()

        if (response?.buttonData == ButtonBar.ButtonData.YES)
        {
            editor.editor.bundle.bundleOperation.deleteMatchingGateway(mapObject)
        }

        editor.setObject(mapObject.position, null)
    }

    override fun contains(editor: MapEditor, mapObject: GatewayObject, point: Point): Boolean
    {
        return mapObject.position == point
    }

    override fun draw(editor: MapEditor, mapObject: GatewayObject, g: Graphics2D, point: Point)
    {
        g.drawImage(image, 0, 0, null)
    }
}