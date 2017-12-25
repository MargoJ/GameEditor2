package pl.margoj.editor2.editor.item.renderer

import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.graphics.GraphicResource
import pl.margoj.mrf.item.properties.special.IconProperty
import java.util.*

class IconPropertyRenderer : ItemPropertyRenderer<String, IconProperty, IconPropertyRenderer.IconNode>()
{
    private val nodeCache = WeakHashMap<IconNode, IconProperty>()
    private val valueCache = WeakHashMap<IconNode, String>()

    override val propertyType: Class<IconProperty> = IconProperty::class.java

    override fun createNode(editor: MargoJEditor, property: IconProperty): IconNode
    {
        val node = IconNode(editor, property)
        this.nodeCache.put(node, property)
        return node
    }

    override fun update(property: IconProperty, node: IconNode, value: String)
    {
        this.valueCache.put(node, value)
    }

    override fun convert(property: IconProperty, node: IconNode): String?
    {
        return this.valueCache[node] ?: ""
    }

    inner class IconNode(val editor: MargoJEditor, val property: IconProperty) : HBox()
    {
        init
        {
            val preview = Button("Podejrzyj grafikę")
            preview.setOnAction {
                val value = this@IconPropertyRenderer.valueCache[this] ?: return@setOnAction
                editor.itemEditor.showIconPreview(editor.iconPathToIconId(GraphicResource.GraphicCategory.ITEM, value))
            }

            preview.prefWidth = 150.0
            HBox.setHgrow(preview, Priority.NEVER)

            val select = Button("Wybierz grafike")

            select.maxWidth = Double.POSITIVE_INFINITY

            select.setOnAction {
                editor.openGraphicsChoice(GraphicResource.GraphicCategory.ITEM) {
                    this@IconPropertyRenderer.valueCache[this] = this.editor.createIconText(it)
                }
            }

            HBox.setHgrow(select, Priority.ALWAYS)

            this.spacing = 5.0
            this.children.setAll(preview, select)
        }
    }
}