package pl.margoj.editor2.editor.map.tools

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.input.MouseButton
import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEvent
import pl.margoj.editor2.editor.map.objects.ObjectTool
import pl.margoj.editor2.editor.map.objects.ObjectTools
import pl.margoj.editor2.geometry.RectangleSelection
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.MapObject

class ObjectTool(val mapEditor: MapEditor) : Tool
{
    private var lastObject: ObjectTool<*>? = null
    override val cursorShape: Selection = RectangleSelection(1, 1, 1, 1)

    override fun mousePressed(event: AdvancedMouseEvent)
    {
    }

    override fun mouseDragged(event: AdvancedMouseEvent)
    {
    }

    override fun mouseReleased(event: AdvancedMouseEvent)
    {
        val map = this.mapEditor.currentMap ?: return
        val currentPoint = this.mapEditor.canvasPointToRendererPoint(event.currentPoint / this.mapEditor.renderer.pointSize)
        val currentObject = map.getObject(currentPoint)

        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (event.button)
        {
            MouseButton.PRIMARY ->
            {
                this.editElement(map, currentPoint)
            }
            MouseButton.MIDDLE ->
            {
                this.editElement(map, currentPoint, true)
            }
            MouseButton.SECONDARY ->
            {
                val contextMenu = ContextMenu()

                if (currentObject == null)
                {
                    val addObject = MenuItem("Dodaj obiekt")
                    addObject.setOnAction {
                        this.editElement(map, currentPoint)
                    }
                    contextMenu.items.add(addObject)
                }
                else
                {
                    val editObject = MenuItem("Edytuj obiekt: ${ObjectTools.getForRaw(currentObject)!!.name}")
                    editObject.setOnAction {
                        this.editElement(map, currentPoint)
                    }
                    contextMenu.items.add(editObject)

                    val deleteObject = MenuItem("Usuń obiekt: ${ObjectTools.getForRaw(currentObject)!!.name}")
                    deleteObject.setOnAction {
                        this.deleteDialog(ObjectTools.getForRaw(currentObject)!!, currentPoint)
                    }
                    contextMenu.items.add(deleteObject)
                }

                contextMenu.show(this.mapEditor.gui.editorGUI.scene.stage, event.event.screenX, event.event.screenY)
            }
        }
    }

    private fun editElement(map: MargoMap, currentPoint: Point, useLast: Boolean = false)
    {
        if(useLast && this.lastObject != null)
        {
            this.editDialog(this.lastObject!!, currentPoint)
            return
        }

        val currentObject = map.getObject(currentPoint)

        if (currentObject == null)
        {
            val objectChoices = ObjectTools.tools.map { it.name to it }.toMap()

            this.mapEditor.editor.gui.showSelectDialog("Wybierz obiekt: ", objectChoices) { tool ->
                this.editDialog(tool, currentPoint)
            }
        }
        else
        {
            this.editDialog(ObjectTools.getForRaw(currentObject)!!, currentPoint)
        }
    }

    private fun editDialog(tool: ObjectTool<*>, point: Point)
    {
        @Suppress("UNCHECKED_CAST")
        tool as ObjectTool<MapObject<*>>

        tool.edit(this.mapEditor, this.mapEditor.currentMap!!, point, this.mapEditor.currentMap!!.getObject(point))

        this.lastObject = tool
    }

    private fun deleteDialog(tool: ObjectTool<*>, point: Point)
    {
        @Suppress("UNCHECKED_CAST")
        tool as ObjectTool<MapObject<*>>

        tool.delete(this.mapEditor, this.mapEditor.currentMap!!, this.mapEditor.currentMap!!.getObject(point)!!)
    }
}