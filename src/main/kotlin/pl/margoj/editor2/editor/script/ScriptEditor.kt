package pl.margoj.editor2.editor.script

import javafx.application.Platform
import javafx.stage.Modality
import javafx.stage.Stage
import pl.margoj.editor2.EditorApplication
import pl.margoj.editor2.app.scene.ScriptScene
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.operation.LoadResourceOperation
import pl.margoj.mrf.ResourceView
import pl.margoj.mrf.script.NpcScript
import pl.margoj.mrf.script.serialization.NpcScriptDeserializer

class ScriptEditor(val editor: MargoJEditor)
{
    val highlighter = ScriptEditorHighlighter()

    init
    {
        val KEYWORDS = arrayOf(
                "npc",
                "dialog", "opcja", "ustaw", "wykonaj", "zamknij",
                "i", "lub", "oraz", "nie",
                "prawda", "fałsz",
                "jeżeli", "przeciwnie", "dopóki", "każdy", "w",
                "dodaj", "odejmij", "pomnóż", "podziel",
                "posiada", "dodaj", "zabierz", "dodaj", "zabierz", "złoto", "xp",
                "rozpocznij", "walkę", "teleportuj", "na", "koordynaty", "mape", "do", "zabij"
        )
        val PROPERTY_PATTERN = "[\\p{L}0-9_.]+"

        this.highlighter.registerPattern("comment", "(--[^\n]*)")
        this.highlighter.registerPattern("variable_property", "!$PROPERTY_PATTERN")
        this.highlighter.registerPattern("string", "\"([^\"\\\\]|\\\\.)*\"")
        this.highlighter.registerPattern("custom_property", "@$PROPERTY_PATTERN")
        this.highlighter.registerPattern("system_property", "%$PROPERTY_PATTERN")
        this.highlighter.registerPattern("keyword", "\\b(${KEYWORDS.joinToString("|")})\\b")
        this.highlighter.registerPattern("number", "\\d+")

        this.highlighter.compile()
    }

    fun edit(resourceView: ResourceView)
    {
        val deserializer = NpcScriptDeserializer()
        deserializer.fileName = resourceView.id
        this.editor.startOperation(LoadResourceOperation(this.editor, resourceView, deserializer, { script ->
            Platform.runLater {
                this.edit(script!!)
            }
        }))
    }

    fun edit(npcScript: NpcScript)
    {
        val scene = ScriptScene(this, npcScript)
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.initOwner(this.editor.gui.controller.scene.stage)
        scene.stage = stage
        scene.load(EditorApplication::class.java.classLoader)
        stage.show()
    }

    fun requestNewScript()
    {
        FXUtils.loadDialog(EditorApplication::class.java.classLoader, "script/new", "Dodaj nowy skrypt", this.editor.gui.scene.stage, this.editor)
    }
}