package pl.margoj.editor2.editor.map.undo

import pl.margoj.editor2.editor.map.MapEditor

class MapNameChange(val editor: MapEditor, val oldName: String, val newName: String) : UndoAction
{
    override fun undo()
    {
        this.setName(this.oldName)
    }

    override fun redo()
    {
        this.setName(this.newName)
    }

    private fun setName(name: String)
    {
        this.editor.currentMap!!.name = name
        this.editor.gui.update()
    }
}

