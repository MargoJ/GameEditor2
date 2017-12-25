package pl.margoj.editor2.editor.map.undo

import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point

class CollisionsChangeAction(val editor: MapEditor, val map: MargoMap, val oldCollisions: HashMap<Point, Boolean>, val newCollisions: HashMap<Point, Boolean>) : UndoAction
{
    override fun undo()
    {
        this.set(this.oldCollisions)
    }

    override fun redo()
    {
        this.set(this.newCollisions)
    }

    private fun set(collisions: HashMap<Point, Boolean>)
    {
        for ((point, value) in collisions)
        {
            this.map.setCollisionAt(point, value)
        }

        this.editor.renderer.resetCacheAndRedraw(Selection(collisions.keys))
    }
}