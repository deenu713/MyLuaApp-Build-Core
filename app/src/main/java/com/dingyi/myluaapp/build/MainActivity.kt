package com.dingyi.myluaapp.build

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.dingyi.myluaapp.build.api.R
import com.dingyi.terminal.TerminalSession
import com.dingyi.terminal.TerminalSessionClient
import com.dingyi.terminal.view.TerminalView
import com.dingyi.terminal.view.TerminalViewClient
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.configuration.ConsoleOutput
import org.gradle.api.logging.configuration.ShowStacktrace
import org.gradle.api.logging.configuration.WarningMode
import org.gradle.launcher.TestGradleLauncher
import java.io.File
import java.lang.Exception
import java.util.zip.ZipFile
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {


    private lateinit var terminalView: TerminalView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.main)

        terminalView = findViewById(R.id.terminalView)
        terminalView.setBackgroundColor(0xff000000.toInt())
        createTerminal()

        thread {
            extractProjectFromApk()
           /* runGradle()*/
        }
    }

    private fun createTerminal() {

        val session = TerminalSession(
            "gradle",
            File(getDefaultProjectDir(), "TestProject").path,
            arrayOf("help"),
            arrayOfNulls(0),
            0,
            object :
                TerminalSessionClient {
                override fun onTextChanged(changedSession: TerminalSession) {
                    terminalView
                        .postInvalidate()
                }

                override fun onTitleChanged(changedSession: TerminalSession) {

                }

                override fun onSessionFinished(finishedSession: TerminalSession) {

                }

                override fun onCopyTextToClipboard(
                    session: TerminalSession,
                    text: String?
                ) {

                }

                override fun onPasteTextFromClipboard(session: TerminalSession?) {

                }

                override fun onBell(session: TerminalSession) {

                }

                override fun onColorsChanged(session: TerminalSession) {
                    terminalView
                        .postInvalidate()
                }

                override fun onTerminalCursorStateChange(state: Boolean) {
                    terminalView
                        .postInvalidate()
                }

                override fun setTerminalShellPid(
                    session: TerminalSession,
                    pid: Int
                ) {

                }

                override fun getTerminalCursorStyle(): Int {
                    return 0
                }

                override fun logError(tag: String?, message: String?) {

                }

                override fun logWarn(tag: String?, message: String?) {

                }

                override fun logInfo(tag: String?, message: String?) {

                }

                override fun logDebug(tag: String?, message: String?) {

                }

                override fun logVerbose(tag: String?, message: String?) {

                }

                override fun logStackTraceWithMessage(
                    tag: String?,
                    message: String?,
                    e: Exception?
                ) {

                }

                override fun logStackTrace(tag: String?, e: Exception?) {

                }

            }
        )



        terminalView.setTextSize(30)
        terminalView.setTerminalViewClient(object :
            TerminalViewClient {
            override fun onScale(scale: Float): Float {
                terminalView.updateSize()
                return scale
            }

            override fun onSingleTapUp(e: MotionEvent) {
               
            }

            override fun shouldBackButtonBeMappedToEscape(): Boolean {
               return false
            }

            override fun shouldEnforceCharBasedInput(): Boolean {
               return false
            }

            override fun shouldUseCtrlSpaceWorkaround(): Boolean {
               return false
            }

            override fun isTerminalViewSelected(): Boolean {
                return false
            }

            override fun copyModeChanged(copyMode: Boolean) {
               
            }

            override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
                return false
            }

            override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
                return false
            }

            override fun onLongPress(event: MotionEvent?): Boolean {
                return false
            }

            override fun readControlKey(): Boolean {
                return false
            }

            override fun readAltKey(): Boolean {
                return false
            }

            override fun readShiftKey(): Boolean {
                return false
            }

            override fun readFnKey(): Boolean {
                return false
            }

            override fun onCodePoint(
                codePoint: Int,
                ctrlDown: Boolean,
                session: TerminalSession?
            ): Boolean {
                return false
            }

            override fun onEmulatorSet() {
                
            }

            override fun logError(tag: String?, message: String?) {
               
            }

            override fun logWarn(tag: String?, message: String?) {
               
            }

            override fun logInfo(tag: String?, message: String?) {
               
            }

            override fun logDebug(tag: String?, message: String?) {
               
            }

            override fun logVerbose(tag: String?, message: String?) {
               
            }

            override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {
               
            }

            override fun logStackTrace(tag: String?, e: Exception?) {
               
            }

        })


        terminalView.attachSession(session)
        terminalView.updateSize()


        /*  terminalView
              .mEmulator
              .append("Hello World".toByteArray(), 0)
          terminalView.postInvalidate()*/
    }

    private fun extractProjectFromApk() {
        val apkFile = File(packageCodePath)
        val extractDir = getDefaultProjectDir()
        extractDir.mkdirs()
        val zipFile = ZipFile(apkFile)
        zipFile.entries().asSequence()
            .filter {
                it.name.startsWith("TestProject/")
            }.forEach {
                val file = File(extractDir, it.name)
                println(file)
                file.parentFile?.mkdirs()
                file.createNewFile()
                zipFile.getInputStream(it).use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

        zipFile.close()
    }

    private fun getDefaultProjectDir(): File {
        return checkNotNull(getExternalFilesDir(null)) {
            "External files dir is null"
        }
    }



}