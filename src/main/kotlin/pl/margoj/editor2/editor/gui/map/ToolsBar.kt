package pl.margoj.editor2.editor.gui.map

import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCharacterCombination
import pl.margoj.utils.javafx.utils.IconUtils
import pl.margoj.editor2.editor.gui.EditorGUI

class ToolsBar(val gui: EditorGUI)
{
    private val toggleGroup = ToggleGroup()

    fun init(vararg buttons: RadioButton)
    {
        buttons.forEachIndexed { i, button ->
            val accelerator: KeyCharacterCombination
            val icon: String
            val tooltip: String

            when (i)
            {
                0 ->
                {
                    accelerator = KeyCharacterCombination("A")
                    icon = "select"
                    tooltip = "Zaznacz (A)"
                }
                1 ->
                {
                    accelerator = KeyCharacterCombination("S")
                    icon = "paint"
                    tooltip = "Pędzel (S)"
                }
                2 ->
                {
                    accelerator = KeyCharacterCombination("D")
                    icon = "eraser"
                    tooltip = "Gumka (D)"
                }
                3 ->
                {
                    accelerator = KeyCharacterCombination("Z")
                    icon = "fill"
                    tooltip = "Wypełnij (Z)"
                }
                4 ->
                {
                    accelerator = KeyCharacterCombination("X")
                    icon = "object"
                    tooltip = "Obiekt (X)"
                }

                else -> throw IllegalStateException("Illegal tool provided")
            }

            this.gui.scene.scene.accelerators.put(accelerator, Runnable { button.fire() })
            IconUtils.createBinding(button.graphicProperty(), button.selectedProperty(), "tools/" + icon)
            IconUtils.addTooltip(button, tooltip)
            IconUtils.removeDefaultClass(button, "radio-button")

            button.toggleGroup = this.toggleGroup
            button.text = ""

            button.setOnAction {
                this.gui.editor.mapEditor.selectTool(i)
            }
        }

        buttons[1].isSelected = true
    }
}