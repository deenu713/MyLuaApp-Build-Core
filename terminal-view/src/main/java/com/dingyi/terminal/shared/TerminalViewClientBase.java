package com.dingyi.terminal.shared;

import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dingyi.terminal.emulator.Logger;
import com.dingyi.terminal.emulator.TerminalSession;
import com.dingyi.terminal.emulator.TerminalSessionClient;
import com.dingyi.terminal.view.TerminalViewClient;

public class TerminalViewClientBase implements TerminalViewClient {

    @Override
    public float onScale(float scale) {
        return 0;
    }

    @Override
    public void onSingleTapUp(MotionEvent e) {

    }

    @Override
    public boolean shouldBackButtonBeMappedToEscape() {
        return false;
    }

    @Override
    public boolean shouldEnforceCharBasedInput() {
        return false;
    }

    @Override
    public boolean shouldUseCtrlSpaceWorkaround() {
        return false;
    }

    @Override
    public boolean isTerminalViewSelected() {
        return true;
    }

    @Override
    public void copyModeChanged(boolean copyMode) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e, TerminalSession session) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent e) {
        return false;
    }

    @Override
    public boolean onLongPress(MotionEvent event) {
        return false;
    }

    @Override
    public boolean readControlKey() {
        return false;
    }

    @Override
    public boolean readAltKey() {
        return false;
    }

    @Override
    public boolean readShiftKey() {
        return false;
    }

    @Override
    public boolean readFnKey() {
        return false;
    }

    @Override
    public boolean onCodePoint(int codePoint, boolean ctrlDown, TerminalSession session) {
        return false;
    }

    @Override
    public void onEmulatorSet() {

    }

    @Override
    public void logError(String tag, String message) {
        Logger.logError(null, tag, message);
    }

    @Override
    public void logWarn(String tag, String message) {
        Logger.logWarn(null, tag, message);
    }

    @Override
    public void logInfo(String tag, String message) {
        Logger.logInfo(null, tag, message);
    }

    @Override
    public void logDebug(String tag, String message) {
        Logger.logDebug(null, tag, message);
    }

    @Override
    public void logVerbose(String tag, String message) {
        Logger.logVerbose(null, tag, message);
    }

    @Override
    public void logStackTraceWithMessage(String tag, String message, Exception e) {
        Logger.logStackTraceWithMessage(null, tag, message, e);
    }

    @Override
    public void logStackTrace(String tag, Exception e) {
        logStackTraceWithMessage(tag, e.getMessage(), e);
    }
}
