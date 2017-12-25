package pl.margoj.editor2.editor.map.undo

interface UndoAction
{
    fun undo()

    fun redo()
}