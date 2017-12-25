package pl.margoj.editor2.editor.item.renderer

import javafx.scene.Node
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.item.ItemProperty

abstract class ItemPropertyRenderer<O, P : ItemProperty<O>, N : Node>
{
    lateinit var editor: MargoJEditor
        internal set

    abstract val propertyType: Class<P>

    abstract fun createNode(editor: MargoJEditor, property: P): N

    abstract fun update(property: P, node: N, value: O)

    abstract fun convert(property: P, node: N): O?

    protected fun error(message: String)
    {
        QuickAlert
                .create()
                .error()
                .header("Bład poczas walidacji statystyk przedmiotu")
                .content(message)
                .showAndWait()
    }
}