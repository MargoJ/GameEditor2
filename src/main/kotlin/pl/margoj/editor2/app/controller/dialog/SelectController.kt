package pl.margoj.editor2.app.controller.dialog

import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.text.Text
import org.apache.commons.lang3.StringUtils
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import java.net.URL
import java.util.ResourceBundle
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

class SelectController<T> : CustomController
{
    private lateinit var scene: CustomScene<*>
    private lateinit var listContent: MutableList<Text>

    @FXML
    lateinit var list: ListView<Text>

    @FXML
    lateinit var fieldSearch: TextField

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    @Suppress("UNCHECKED_CAST")
    override fun loadData(data: Any)
    {
        val (options, callback) = data as Pair<Map<String, T>, (T) -> Unit>

        this.listContent = ArrayList(options.size)

        for ((option, value) in options)
        {
            val text = Text(option)
            text.setOnMouseClicked {
                if (it.clickCount == 2)
                {
                    callback(value)
                    this.scene.stage.close()
                }
            }

            this.listContent.add(text)
        }

        this.updateView()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        this.fieldSearch.textProperty().addListener { _, _, _ -> this.updateView() }
    }

    private fun updateView()
    {
        val searchTerm = fieldSearch.text
        val items = ArrayList<Text>()

        for (text in this.listContent)
        {
            if (StringUtils.containsIgnoreCase(text.text, searchTerm))
            {
                items.add(text)
            }
        }

        this.list.items.setAll(items)
    }
}