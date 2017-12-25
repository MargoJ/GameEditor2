package pl.margoj.editor2.editor.map.tools

import pl.margoj.editor2.editor.map.mouseevents.AdvancedMouseEventListener
import pl.margoj.editor2.geometry.Selection

interface Tool : AdvancedMouseEventListener
{
    val cursorShape: Selection
}