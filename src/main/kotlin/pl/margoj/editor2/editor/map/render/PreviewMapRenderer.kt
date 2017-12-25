package pl.margoj.editor2.editor.map.render

import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.geometry.Selection
import pl.margoj.mrf.map.MargoMap

class PreviewMapRenderer(editor: MargoJEditor, override val currentMap: MargoMap?) : MapRenderer(editor)
{
    override var pointSize: Int = 32

    override val currentLayer: Int = 0

    override val shouldDrawWater: Boolean = false

    override val shouldDrawGrid: Boolean = false

    override val shouldDrawCollisions: Boolean = false

    override val drawOnlyCurrentLayer: Boolean = false

    override val selection: Selection? = null

    override var cursorSelection: Selection? = null
}