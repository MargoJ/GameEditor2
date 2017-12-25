package pl.margoj.editor2.editor.item.renderer

import javafx.scene.Node
import javafx.scene.control.ComboBox
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.item.ItemProperty
import java.util.Collections
import java.util.WeakHashMap

abstract class ListPropertyRenderer<O, P : ItemProperty<O>> : ItemPropertyRenderer<O, P, ComboBox<String>>()
{
    private val programaticllyChangedNodes = Collections.newSetFromMap(WeakHashMap<Node, Boolean>())

    abstract fun getAllValues(): Array<O>

    abstract fun getStringRepresentation(value: O): String

    override fun createNode(editor: MargoJEditor, property: P): ComboBox<String>
    {
        val box = ComboBox<String>()
        box.maxWidth = Double.POSITIVE_INFINITY

        for (value in this.getAllValues())
        {
            box.items.add(this.getStringRepresentation(value))
        }

        box.selectionModel.select(0)

        return box
    }

    override fun update(property: P, node: ComboBox<String>, value: O)
    {
        node.selectionModel.select(this.getStringRepresentation(value))
    }

    override fun convert(property: P, node: ComboBox<String>): O?
    {
        return convertFromString(node.selectionModel.selectedItem)
    }

    fun convertFromString(string: String): O?
    {
        return this.getAllValues().find { this.getStringRepresentation(it) == string }
    }
}