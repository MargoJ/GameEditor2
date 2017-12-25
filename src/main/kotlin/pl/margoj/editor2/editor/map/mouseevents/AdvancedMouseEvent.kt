package pl.margoj.editor2.editor.map.mouseevents

import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import pl.margoj.mrf.map.Point

data class AdvancedMouseEvent
(
        val event: MouseEvent,
        val button: MouseButton,
        val startingPoint: Point,
        val currentPoint: Point,
        val dragged: Boolean
)