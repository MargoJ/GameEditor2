package pl.margoj.editor2.editor.map.undo

import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.fragment.MapFragment

class TilesChangeAction(val editor: MapEditor, val map: MargoMap, val oldFragments: Collection<MapFragment>, val newFragments: Collection<MapFragment>) : UndoAction
{
    override fun undo()
    {
        this.set(this.oldFragments)
    }

    override fun redo()
    {
        this.set(this.newFragments)
    }

    private fun set(collection: Collection<MapFragment>)
    {
        val redraws = HashSet<Point>()

        for (fragment in collection)
        {
            map.setFragment(fragment)
            redraws.add(fragment.point)
            redraws.addAll(fragment.point.getNeighborhood(true))
        }

        this.editor.renderer.resetCacheAndRedraw(Selection(redraws))
    }
}