package org.gradle.internal.nativeintegration.console;

import javax.annotation.Nullable;

public class VirtualProcessConsoleDetector implements ConsoleDetector {

    private final ConsoleMetaData virtualProcessConsoleMetaData = new VirtualProcessConsoleMetaData();

    @Nullable
    @Override
    public ConsoleMetaData getConsole() {
        return virtualProcessConsoleMetaData;
    }

    @Override
    public boolean isConsoleInput() {
        return false;
    }
}
