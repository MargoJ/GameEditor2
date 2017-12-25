package pl.margoj.editor2.editor.map.undo

import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point

class WaterChangeAction(val editor: MapEditor, val map: MargoMap, val oldWater: HashMap<Point, Int>, val newWater: HashMap<Point, Int>) : UndoAction
{
    override fun undo()
    {
        this.set(this.oldWater)
    }

    override fun redo()
    {
        this.set(this.newWater)
    }

    private fun set(water: HashMap<Point, Int>)
    {
        for ((point, value) in water)
        {
            this.map.setWaterLevelAt(point, value)
        }

        this.editor.renderer.resetCacheAndRedraw(Selection(water.keys))
    }
}