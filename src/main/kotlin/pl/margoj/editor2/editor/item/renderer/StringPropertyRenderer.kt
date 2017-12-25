package pl.margoj.editor2.editor.item.renderer

import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.item.properties.StringProperty

class StringPropertyRenderer : ItemPropertyRenderer<String, StringProperty, TextInputControl>()
{
    override val propertyType: Class<StringProperty> = StringProperty::class.java

    override fun createNode(editor: MargoJEditor, property: StringProperty): TextInputControl
    {
        val control: TextInputControl

        if (property.long)
        {
            control = TextArea()
            control.prefHeight = 100.0
            control.minHeight = 100.0
        }
        else
        {
            control = TextField()
        }

        control.text = property.default

        return control
    }

    override fun update(property: StringProperty, node: TextInputControl, value: String)
    {
        node.text = value
    }

    override fun convert(property: StringProperty, node: TextInputControl): String?
    {
        val text = node.text
        if (property.regexp == null)
        {
            return text
        }

        if (!property.regexp!!.matches(text))
        {
            error("Wartość dla '$${property.name}' ($text) nie spełnia wymogów (${property.regexp.toString()})")
            return null
        }

        return text
    }
}