package pl.margoj.editor2.editor.item.renderer

import javafx.scene.control.TextField
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.item.properties.DoubleProperty

class DoublePropertyRenderer : ItemPropertyRenderer<Double, DoubleProperty, TextField>()
{
    override val propertyType: Class<DoubleProperty> = DoubleProperty::class.java

    override fun createNode(editor: MargoJEditor, property: DoubleProperty): TextField
    {
        val textField = TextField()
        FXUtils.makeNumberField(textField, true)
        textField.text = property.default.toString()
        return textField
    }

    override fun update(property: DoubleProperty, node: TextField, value: Double)
    {
        node.text = value.toString()
    }

    override fun convert(property: DoubleProperty, node: TextField): Double?
    {
        val doubleValue: Double
        try
        {
            doubleValue = node.text.toDouble()
        }
        catch (e: NumberFormatException)
        {
            error("Wartość dla '${property.name}' (${node.text}) nie jest liczbą!")
            return null
        }

        if (doubleValue < property.minimum)
        {
            error("Wartość dla '${property.name}' ($doubleValue) jest mniejsza od wartośći minimalnej (${property.minimum})")
            return null
        }

        return doubleValue
    }
}