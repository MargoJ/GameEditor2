package pl.margoj.editor2

import javafx.application.Application
import javafx.stage.Stage
import joptsimple.OptionParser
import joptsimple.OptionSet
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.appender.FileAppender
import org.apache.logging.log4j.core.layout.PatternLayout
import pl.margoj.editor2.app.scene.StartScene
import pl.margoj.editor2.editor.DebuggingProfile
import pl.margoj.editor2.utils.FileUtils
import pl.margoj.utils.commons.logging.Log4j2OutputStream
import pl.margoj.utils.javafx.utils.IconUtils
import java.io.File
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date

var EDITOR_DEBUGGING: Boolean = false

var DEBUGGING_PROFILE: DebuggingProfile? = null

fun main(args: Array<String>)
{
    val optionParser = OptionParser()

    optionParser.acceptsAll(Arrays.asList("help", "?"), "Wyświetla tą pomoc")
    optionParser.acceptsAll(Arrays.asList("debug", "d"), "Włącza tryb debuggowania")
    optionParser.acceptsAll(Arrays.asList("debuggingprofile", "p"), "Profile debugowania").withRequiredArg().defaultsTo("")

    val options: OptionSet

    try
    {
        options = optionParser.parse(*args)

        if (options.has("?"))
        {
            optionParser.printHelpOn(System.out)
            return
        }

        if (options.has("debug"))
        {
            EDITOR_DEBUGGING = true
        }
    }
    catch (e: Exception)
    {
        e.printStackTrace()
        return
    }

    FileUtils.ensureDirectoryCreationIfAvailable(FileUtils.LOGS_DIRECTORY)
    val file = File(FileUtils.LOGS_DIRECTORY, "log-${SimpleDateFormat("y-M-d-H-s-S").format(Date())}.log")

    val builder = FileAppender.Builder::class.java.newInstance() as FileAppender.Builder<*>
    val appender = builder
            .withFileName(file.absolutePath)
            .withName("File")
            .withLayout(PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss}] [%c] %5level: %msg%n").build())
            .build()
    appender.start()

    val logger = LogManager.getRootLogger() as Logger
    logger.addAppender(appender)

    logger.info("Logger file is: ${file.absolutePath}")

    System.setOut(PrintStream(Log4j2OutputStream(LogManager.getLogger("STDOUT"), Level.INFO)))
    System.setErr(PrintStream(Log4j2OutputStream(LogManager.getLogger("STDERR"), Level.ERROR)))

    DEBUGGING_PROFILE = DebuggingProfile(options.valueOf("debuggingprofile") as String)

    IconUtils.iconClassLoaderSource = EditorApplication::class.java.classLoader

    logger.info("Launching EditorApplication")
    Application.launch(EditorApplication::class.java, *args)
}

class EditorApplication : Application()
{
    override fun start(primaryStage: Stage)
    {
        val scene = StartScene()

        scene.stage = primaryStage
        scene.load(EditorApplication::class.java.classLoader)
    }
}