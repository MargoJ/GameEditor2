package pl.margoj.editor2.editor.map.undo

import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.mrf.map.metadata.MetadataElement

class MetadataUndoRedo(val editor: MapEditor, val old: Collection<MetadataElement>, val new: Collection<MetadataElement>) : UndoAction
{
    override fun redo()
    {
        this.editor.currentMap!!.metadata = new
    }

    override fun undo()
    {
        this.editor.currentMap!!.metadata = old
    }
}