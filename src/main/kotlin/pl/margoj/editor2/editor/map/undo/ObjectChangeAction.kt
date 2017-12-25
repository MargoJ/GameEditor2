package pl.margoj.editor2.editor.map.undo

import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.editor.map.objects.ObjectTool
import pl.margoj.editor2.editor.map.objects.ObjectTools
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.objects.MapObject

class ObjectChangeAction(val editor: MapEditor, val oldObject: MapObject<*>?, val newObject: MapObject<*>?) : UndoAction
{
    override fun undo()
    {
        this.set(this.newObject, this.oldObject)
    }

    override fun redo()
    {
        this.set(this.oldObject, this.newObject)
    }

    @Suppress("UNCHECKED_CAST")
    private fun set(from: MapObject<*>?, to: MapObject<*>?)
    {
        if(from == null && to == null)
        {
            return
        }

        val map = this.editor.currentMap!!

        if (from != null)
        {
            val oldTool = ObjectTools.getForRaw(from) as ObjectTool<MapObject<*>>

            map.deleteObject(from.position)

            this.editor.renderer.resetCacheAndRedraw(Selection(oldTool.getPointsContaining(this.editor, from, map)))
        }

        if(to != null)
        {
            val newTool = ObjectTools.getForRaw(to) as ObjectTool<MapObject<*>>

            map.addObject(to)

            this.editor.renderer.resetCacheAndRedraw(Selection(newTool.getPointsContaining(this.editor, to, map)))
        }
        else
        {
            map.deleteObject(from!!.position)
        }
    }
}