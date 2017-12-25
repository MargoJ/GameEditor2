package pl.margoj.editor2.editor.item.renderer

import javafx.scene.control.TextField
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.item.properties.special.CooldownProperty

class CooldownPropertyRenderer : ItemPropertyRenderer<CooldownProperty.Cooldown, CooldownProperty, TextField>()
{
    override val propertyType: Class<CooldownProperty> = CooldownProperty::class.java

    override fun createNode(editor: MargoJEditor, property: CooldownProperty): TextField
    {
        val textField = TextField()
        FXUtils.makeNumberField(textField, true)
        textField.text = property.default.toString()
        return textField
    }

    override fun update(property: CooldownProperty, node: TextField, value: CooldownProperty.Cooldown)
    {
        node.text = value.cooldown.toString()
    }

    override fun convert(property: CooldownProperty, node: TextField): CooldownProperty.Cooldown?
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

        return CooldownProperty.Cooldown(intValue, 0L)
    }
}