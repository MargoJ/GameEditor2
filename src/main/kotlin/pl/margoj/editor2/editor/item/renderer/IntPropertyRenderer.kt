package pl.margoj.editor2.editor.item.renderer

import javafx.scene.control.TextField
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.item.properties.IntProperty

class IntPropertyRenderer : ItemPropertyRenderer<Int, IntProperty, TextField>()
{
    override val propertyType: Class<IntProperty> = IntProperty::class.java

    override fun createNode(editor: MargoJEditor, property: IntProperty): TextField
    {
        val textField = TextField()
        FXUtils.makeNumberField(textField, true)
        textField.text = property.default.toString()
        return textField
    }

    override fun update(property: IntProperty, node: TextField, value: Int)
    {
        node.text = value.toString()
    }

    override fun convert(property: IntProperty, node: TextField): Int?
    {
        val intValue: Int
        try
        {
            intValue = node.text.toInt()
        }
        catch (e: NumberFormatException)
        {
            error("Wartość dla '${property.name}' (${node.text}) nie jest liczbą całkowitą")
            return null
        }

        if (intValue < property.minimum)
        {
            error("Wartość dla '${property.name}' ($intValue) jest mniejsza od wartośći minimalnej (${property.minimum})")
            return null
        }

        return intValue
    }
}