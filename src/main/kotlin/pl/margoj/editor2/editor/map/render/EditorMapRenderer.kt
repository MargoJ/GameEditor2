package pl.margoj.editor2.editor.map.render

import pl.margoj.editor2.editor.map.MapEditor
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap

class EditorMapRenderer(val mapEditor: MapEditor) : MapRenderer(mapEditor.editor)
{
    override var cursorSelection: Selection? = null

    override val pointSize: Int = 32

    override val selection: Selection?
        get() = this.mapEditor.selection

    override val currentMap: MargoMap?
        get() = this.mapEditor.currentMap

    override val drawOnlyCurrentLayer: Boolean
        get() = this.mapEditor.drawOnlyCurrentLayer

    override val currentLayer: Int
        get() = this.mapEditor.currentLayer

    override val shouldDrawCollisions: Boolean
        get() = this.mapEditor.currentLayer == MargoMap.COLLISION_LAYER

    override val shouldDrawWater: Boolean
        get() = this.mapEditor.currentLayer == MargoMap.WATER_LAYER

    override val shouldDrawGrid: Boolean
        get() = this.mapEditor.showGrid
}