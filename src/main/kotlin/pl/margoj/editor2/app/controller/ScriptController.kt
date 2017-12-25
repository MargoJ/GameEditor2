package pl.margoj.editor2.app.controller

import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import org.fxmisc.richtext.model.StyleSpans
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.utils.javafx.utils.QuickAlert
import pl.margoj.editor2.editor.script.ScriptEditor
import pl.margoj.mrf.script.NpcScript
import pl.margoj.mrf.script.serialization.NpcScriptSerializer
import java.io.ByteArrayInputStream
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScriptController : CustomController
{
    private lateinit var editor: ScriptEditor
    private lateinit var script: NpcScript

    @FXML
    lateinit var container: VBox

    @FXML
    lateinit var codeArea: CodeArea

    @FXML
    lateinit var buttonSave: Button

    override fun loadData(data: Any)
    {
        @Suppress("UNCHECKED_CAST")
        data as Pair<ScriptEditor, NpcScript>

        this.editor = data.first
        this.script = data.second

        this.codeArea.replaceText(0, this.codeArea.text.length, this.script.content)
    }

    override fun preInit(scene: CustomScene<*>)
    {
        scene.scene.stylesheets.add(ScriptController::class.java.classLoader.getResource("css/syntax.css").toExternalForm())

        scene.stage.setOnCloseRequest {
            val result = QuickAlert.create()
                    .confirmation()
                    .header("Czy chcesz zapisać?")
                    .content("Czy chcesz zapisać aktualny skrypt?")
                    .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO), ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE))
                    .showAndWait()

            when (result?.buttonData)
            {
                ButtonBar.ButtonData.YES ->
                {
                    this.buttonSave.fire()
                }
                ButtonBar.ButtonData.NO ->
                {
                }
                else -> it.consume()
            }
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        this.codeArea = CodeArea()

        VBox.setVgrow(this.codeArea, Priority.ALWAYS)
        VBox.setMargin(this.codeArea, Insets(5.0, 10.0, 10.0, 10.0))

        this.codeArea.paragraphGraphicFactory = LineNumberFactory.get(codeArea)

        this.codeArea.richChanges()
                .filter { ch -> ch.inserted != ch.removed }
                .supplyTask { this.computeHighlightingAsync(codeArea) }
                .await()
                .filterMap {
                    if (it.isSuccess)
                    {
                        Optional.of(it.get())
                    }
                    else
                    {
                        it.failure.printStackTrace()
                        Optional.empty()
                    }
                }
                .subscribe {
                    try
                    {
                        this.script.content = codeArea.text

                        codeArea.setStyleSpans(0, it)
                    }
                    catch (e: IllegalStateException)
                    {
                        // ignored, caused by style being changed when the code changes
                    }
                }

        val cachedException = object : RuntimeException()
        {
            override fun fillInStackTrace(): Throwable
            {
                return this
            }
        }

        Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
            if (e === cachedException)
            {
                return@UncaughtExceptionHandler
            }

            System.err.println("Exception in main application thread")
            e.printStackTrace()
        }

        this.codeArea.richChanges()
                .filter { it.inserted.text == "\n" }
                .subscribe {
                    val current = codeArea.getParagraph(codeArea.currentParagraph + 1)
                    if (current.text.trim().isEmpty())
                    {
                        val previous = codeArea.getParagraph(codeArea.currentParagraph).text.toCharArray()
                        var i = 0
                        val indent = StringBuilder()
                        while (i < previous.size && previous[i] == '\t')
                        {
                            indent.append('\t')
                            i++
                        }

                        codeArea.insertText(codeArea.currentParagraph + 1, 0, indent.toString())
                        codeArea.requestFollowCaret()

                        // don't ever do that, just don't, neither ask why i did that, just don't
                        throw cachedException
                    }
                }

        this.container.children.add(0, this.codeArea)

        this.buttonSave.setOnAction {
            this.script.content = this.codeArea.text

            this.editor.editor.bundle.saveResource(this.script, ByteArrayInputStream(NpcScriptSerializer().serialize(this.script)))
            this.editor.editor.reloadResourceIndex()

            QuickAlert.create().information().header("Zapisano!").content("Skrypt: ${script.id} został zapisany poprawnie!").showAndWait()
        }
    }

    private val executor: ExecutorService = Executors.newSingleThreadExecutor {
        val thread = Thread(it, "AsyncHighlighterThread")
        thread.isDaemon = true
        thread
    }

    fun computeHighlightingAsync(area: CodeArea): Task<StyleSpans<Collection<String>>>
    {
        val task = object : Task<StyleSpans<Collection<String>>>()
        {
            override fun call(): StyleSpans<Collection<String>>
            {
                return this@ScriptController.editor.highlighter.computeHighlighting(area.text)
            }
        }

        executor.execute(task)
        return task
    }
}