package pl.margoj.editor2.editor.gui.map

import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCharacterCombination
import pl.margoj.utils.javafx.utils.IconUtils
import pl.margoj.editor2.editor.gui.EditorGUI
import pl.margoj.mrf.map.MargoMap

class LayerBar(val gui: EditorGUI)
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
                MargoMap.COLLISION_LAYER ->
                {
                    accelerator = KeyCharacterCombination("Q")
                    icon = "layer_c"
                    tooltip = "Warstwa kolizji (Q)"
                }
                MargoMap.WATER_LAYER ->
                {
                    accelerator = KeyCharacterCombination("W")
                    icon = "layer_w"
                    tooltip = "Warstwa wody (W)"
                }
                else ->
                {
                    accelerator = KeyCharacterCombination(if (i == 9) "0" else Integer.toString(i + 1))
                    icon = "layer_" + (i + 1)
                    tooltip = "Warstwa " + (i + 1)
                }
            }

            this.gui.scene.scene.accelerators.put(accelerator, Runnable { button.fire() })
            IconUtils.createBinding(button.graphicProperty(), button.selectedProperty(), "layers/" + icon)
            IconUtils.addTooltip(button, tooltip)
            IconUtils.removeDefaultClass(button, "radio-button")

            button.toggleGroup = this.toggleGroup
            button.text = ""


            button.setOnAction {
                this.gui.editor.mapEditor.currentLayer = i
            }
        }

        buttons[0].isSelected = true
    }
}