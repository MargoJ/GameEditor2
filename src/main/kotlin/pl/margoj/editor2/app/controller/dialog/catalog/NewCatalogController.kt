package pl.margoj.editor2.app.controller.dialog.catalog

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.editor2.editor.operation.NewCatalogOperation
import java.net.URL
import java.util.*

class NewCatalogController : CustomController
{
    lateinit var parent: CatalogController
    lateinit var scene: CustomScene<*>

    @FXML
    lateinit var fieldName: TextField

    @FXML
    lateinit var fieldCatalog: TextField

    @FXML
    lateinit var buttonAdd: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun loadData(data: Any)
    {
        this.parent = data as CatalogController
    }

    private companion object
    {
        val CATALOG_REGEXP = Regex("[a-z0-9_]{1,256}")
    }


    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        this.buttonAdd.setOnAction {
            val errors = ArrayList<String>()

            if (this.fieldName.text.length < 2)
            {
                errors.add("Nazwa musi mieć przynajmniej 2 litery")
            }

            if (this.fieldCatalog.text.isEmpty())
            {
                errors.add("Katalog musi mieć przynajmniej 1 litere")
            }

            if (!this.fieldCatalog.text.matches(CATALOG_REGEXP))
            {
                errors.add("Katalog może składać się tylko ze znaków 0-9, a-z i _")
            }

            val parent = this.parent.parent
            val catalogs = parent.catalogs

            if (catalogs.containsKey(this.fieldName.text))
            {
                errors.add("Katalog z taką nazwą już istnieje")
            }

            if (catalogs.containsValue(this.fieldCatalog.text))
            {
                errors.add("Taki katalog już istnieje")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Nie można dodać katalogu", errors)
                return@setOnAction
            }

            parent.editor.startOperation(NewCatalogOperation(parent.editor, parent.category, this.fieldName.text, this.fieldCatalog.text, {
                Platform.runLater {
                    parent.updateCatalogs()
                }
            }))

            this.scene.stage.close()
        }
    }
}