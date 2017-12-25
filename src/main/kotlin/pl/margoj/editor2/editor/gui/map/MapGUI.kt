package pl.margoj.editor2.editor.gui.map

import javafx.scene.input.KeyCharacterCombination
import javafx.scene.input.KeyCombination
import pl.margoj.editor2.EditorApplication
import pl.margoj.editor2.app.controller.dialog.tool.AbstractToolDialogController
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.IconUtils
import pl.margoj.editor2.editor.gui.EditorGUI
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.operation.LoadTilesetListOperation
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.MapObject

class MapGUI(val editorGUI: EditorGUI)
{
    val mapEditor: MapEditor get() = this.editorGUI.editor.mapEditor

    fun init()
    {
        val controller = this.editorGUI.controller

        controller.buttonSelectTileset.setOnAction {
            this.mapEditor.editor.startOperation(LoadTilesetListOperation(this.mapEditor))
        }

        controller.checkboxShowGrid.selectedProperty().addListener { _, _, newValue ->
            this.mapEditor.showGrid = newValue
        }

        IconUtils.createFullCheckbox(
                checkbox = controller.checkboxShowGrid,
                tooltip = "Pokazuj siatke (G)",
                icon = "grid"
        )

        this.editorGUI.scene.scene.accelerators.put(KeyCharacterCombination("G"), Runnable { controller.checkboxShowGrid.fire() })

        IconUtils.createFullCheckbox(
                checkbox = controller.checkboxDrawCurrentLayer,
                tooltip = "Podświetlaj tylko aktualną warstwe (E)",
                icon = "layer_onlycurrent"
        )

        controller.checkboxDrawCurrentLayer.selectedProperty().addListener { _, _, newValue ->
            this.mapEditor.drawOnlyCurrentLayer = newValue
        }

        this.editorGUI.scene.scene.accelerators.put(KeyCharacterCombination("E"), Runnable { controller.checkboxDrawCurrentLayer.fire() })

        controller.save.setOnAction {
            this.mapEditor.saveMap()
        }

        IconUtils.createFullButton(
                button = controller.save,
                tooltip = "Zapisz (Ctrl+S)",
                icon = "save"
        )

        this.editorGUI.scene.scene.accelerators.put(KeyCharacterCombination("S", KeyCombination.CONTROL_DOWN), Runnable { controller.save.fire() })

        IconUtils.createFullButton(
                button = controller.undo,
                tooltip = "Cofnij (Ctrl+Z)",
                icon = "undo"
        )

        controller.undo.setOnAction {
            this.mapEditor.undo()
        }

        this.editorGUI.scene.scene.accelerators.put(KeyCharacterCombination("Z", KeyCombination.CONTROL_DOWN), Runnable { controller.undo.fire() })

        IconUtils.createFullButton(
                button = controller.redo,
                tooltip = "Powtórz (Ctrl+Y)",
                icon = "redo"
        )

        controller.redo.setOnAction {
            this.mapEditor.redo()
        }

        this.editorGUI.scene.scene.accelerators.put(KeyCharacterCombination("Y", KeyCombination.CONTROL_DOWN), Runnable { controller.redo.fire() })

        IconUtils.createFullButton(
                button = controller.edit,
                tooltip = "Edytuj nazwe i wymiary mapy",
                icon = "edit"
        )

        controller.edit.setOnAction {
            this.mapEditor.editMap()
        }

        IconUtils.createFullButton(
                button = controller.meta,
                tooltip = "Edytuj dane mapy",
                icon = "meta"
        )

        controller.meta.setOnAction {
            this.mapEditor.editMeta()
        }

        IconUtils.createFullButton(
                button = controller.preview,
                tooltip = "Podgląd mapy",
                icon = "preview"
        )

        controller.preview.setOnAction {
            if (this.mapEditor.currentMap != null)
            {
                this.mapEditor.openPreview(this.mapEditor.currentMap!!)
            }
        }

        this.update()
    }

    fun openToolGui(tool: String, map: MargoMap, point: Point, mapObject: MapObject<*>?)
    {
        FXUtils.loadDialog(EditorApplication::class.java.classLoader, "map/tool/$tool", "Edytuj obiekt", this.editorGUI.scene.stage, AbstractToolDialogController.Data(this.mapEditor.editor, map, point, mapObject))
    }

    fun update()
    {
        val controller = this.editorGUI.controller

        val builder = StringBuilder()

        val map = this.mapEditor.currentMap
        if (map != null)
        {
            builder.append(map.name).append(" [").append(map.id).append("]")
        }

        controller.labelMapName.text = builder.toString()
    }
}