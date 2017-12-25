package pl.margoj.editor2.editor.map.undo

import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.geometry.Selection

class SelectionChangeAction(val editor: MapEditor, val oldSelection: Selection, val newSelection: Selection) : UndoAction
{
    override fun undo()
    {
        this.set(this.oldSelection)
    }

    override fun redo()
    {
        this.set(this.newSelection)
    }

    private fun set(selection: Selection)
    {
        this.editor.selection = selection

        this.editor.renderer.resetCacheAndRedraw(this.oldSelection)
        this.editor.renderer.resetCacheAndRedraw(this.newSelection)
    }
}