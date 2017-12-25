package pl.margoj.editor2.editor.item.renderer

import javafx.scene.control.ToggleButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.mrf.item.properties.special.ProfessionRequirementProperty

class ProfessionRequirementPropertyRenderer
    : ItemPropertyRenderer<ProfessionRequirementProperty.ProfessionRequirement, ProfessionRequirementProperty, ProfessionRequirementPropertyRenderer.RequirementNode>()
{
    override val propertyType: Class<ProfessionRequirementProperty> = ProfessionRequirementProperty::class.java

    override fun createNode(editor: MargoJEditor, property: ProfessionRequirementProperty): RequirementNode
    {
        return RequirementNode()
    }

    override fun update(property: ProfessionRequirementProperty, node: RequirementNode, value: ProfessionRequirementProperty.ProfessionRequirement)
    {
        node.warrior.isSelected = value.warrior
        node.paladin.isSelected = value.paladin
        node.bladedancer.isSelected = value.bladedancer
        node.mage.isSelected = value.mage
        node.hunter.isSelected = value.hunter
        node.tracker.isSelected = value.tracker
    }

    override fun convert(property: ProfessionRequirementProperty, node: RequirementNode): ProfessionRequirementProperty.ProfessionRequirement
    {
        return ProfessionRequirementProperty.ProfessionRequirement(
                node.warrior.isSelected, node.paladin.isSelected, node.bladedancer.isSelected,
                node.mage.isSelected, node.hunter.isSelected, node.tracker.isSelected
        )
    }

    inner class RequirementNode : HBox()
    {
        val warrior = ToggleButton("Wojownik")
        val paladin = ToggleButton("Paladyn")
        val bladedancer = ToggleButton("Tancerz ostrzy")
        val mage = ToggleButton("Mag")
        val hunter = ToggleButton("Łowca")
        val tracker = ToggleButton("Tropiciel")

        init
        {
            this.children.setAll(this.warrior, this.paladin, this.bladedancer, this.mage, this.hunter, this.tracker)

            for (child in this.children)
            {
                HBox.setHgrow(child, Priority.ALWAYS)
                child as ToggleButton
                child.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
            }

            this.spacing = 5.0
        }
    }
}