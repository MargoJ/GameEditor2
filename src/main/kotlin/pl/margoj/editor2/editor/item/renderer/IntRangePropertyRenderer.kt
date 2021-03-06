package pl.margoj.editor2.editor.item.renderer

import javafx.scene.control.TextField
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.item.properties.IntRangeProperty

class IntRangePropertyRenderer : ItemPropertyRenderer<IntRange, IntRangeProperty, TextField>()
{
    override val propertyType: Class<IntRangeProperty> = IntRangeProperty::class.java

    override fun createNode(editor: MargoJEditor, property: IntRangeProperty): TextField
    {
        val textField = TextField()
        textField.text = "${property.default.first}-${property.default.endInclusive}"
        return textField
    }

    override fun update(property: IntRangeProperty, node: TextField, value: IntRange)
    {
        node.text = "${value.first}-${value.endInclusive}"
    }

    override fun convert(property: IntRangeProperty, node: TextField): IntRange?
    {
        val intRangeValue: IntRange
        val split = node.text.split("-")

        if (split.size != 2)
        {
            error("Wartość dla ${property.name} nie jest poprawnym zakresem")
            return null
        }

        try
        {
            intRangeValue = split[0].toInt()..split[1].toInt()
        }
        catch (e: NumberFormatException)
        {
            error("Wartość dla '${property.name}' (${node.text}) nie jest liczbą całkowitą")
            return null
        }

        if (intRangeValue.first < property.minimum || intRangeValue.endInclusive < property.minimum)
        {
            error("Wartość dla '${property.name}' (${node.text}) jest mniejsza od wartośći minimalnej (${property.minimum})")
            return null
        }

        return intRangeValue
    }
}