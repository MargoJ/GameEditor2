package pl.margoj.editor2.editor.map.mouseevents

interface AdvancedMouseEventListener
{
    fun mousePressed(event: AdvancedMouseEvent)

    fun mouseDragged(event: AdvancedMouseEvent)

    fun mouseReleased(event: AdvancedMouseEvent)
}