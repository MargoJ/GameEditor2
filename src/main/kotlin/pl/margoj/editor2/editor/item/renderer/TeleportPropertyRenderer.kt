package pl.margoj.editor2.editor.item.renderer

import javafx.scene.Node
import pl.margoj.editor2.EditorApplication
import pl.margoj.editor2.app.controller.parts.TeleportController
import pl.margoj.utils.javafx.utils.FXMLJarLoader
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.item.properties.special.TeleportProperty

class TeleportPropertyRenderer : ItemPropertyRenderer<TeleportProperty.Teleport, TeleportProperty, Node>()
{
    private companion object
    {
        val CONTROLLER_CONSTRAINT = "mjeditor-teleport-controller"
    }

    override val propertyType: Class<TeleportProperty> = TeleportProperty::class.java

    override fun createNode(editor: MargoJEditor, property: TeleportProperty): Node
    {
        val loader = FXMLJarLoader(EditorApplication::class.java.classLoader, "parts/teleport")
        loader.load()

        val controller = loader.controller as TeleportController
        controller.editor = this.editor
        loader.node.properties.put(CONTROLLER_CONSTRAINT, controller)

        return loader.node
    }

    private fun getControllerOf(node: Node): TeleportController = node.properties.get(CONTROLLER_CONSTRAINT) as TeleportController

    override fun update(property: TeleportProperty, node: Node, value: TeleportProperty.Teleport)
    {
        val controller = this.getControllerOf(node)

        controller.buttonToggle.isSelected = value.customCoords
        controller.fieldMap.text = value.map
        controller.fieldX.text = value.x.toString()
        controller.fieldY.text = value.y.toString()
    }

    override fun convert(property: TeleportProperty, node: Node): TeleportProperty.Teleport
    {
        val controller = this.getControllerOf(node)

        return TeleportProperty.Teleport(
                controller.fieldMap.text, controller.buttonToggle.isSelected, controller.fieldX.text.toInt(), controller.fieldY.text.toInt()
        )
    }

}