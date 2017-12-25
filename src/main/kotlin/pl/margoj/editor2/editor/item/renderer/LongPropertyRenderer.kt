package pl.margoj.editor2.editor.item.renderer

import javafx.scene.control.TextField
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.item.properties.LongProperty

class LongPropertyRenderer : ItemPropertyRenderer<Long, LongProperty, TextField>()
{
    override val propertyType: Class<LongProperty> = LongProperty::class.java

    override fun createNode(editor: MargoJEditor, property: LongProperty): TextField
    {
        val textField = TextField()
        FXUtils.makeNumberField(textField, true)
        textField.text = property.default.toString()
        return textField
    }

    override fun update(property: LongProperty, node: TextField, value: Long)
    {
        node.text = value.toString()
    }

    override fun convert(property: LongProperty, node: TextField): Long?
    {
        val longValue: Long
        try
        {
            longValue = node.text.toLong()
        }
        catch (e: NumberFormatException)
        {
            error("Wartość dla '${property.name}' (${node.text}) nie jest liczbą całkowitą")
            return null
        }

        if (longValue < property.minimum)
        {
            error("Wartość dla '${property.name}' ($longValue) jest mniejsza od wartośći minimalnej (${property.minimum})")
            return null
        }

        return longValue
    }
}