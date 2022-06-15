package org.gradle.internal.nativeintegration.console;

import com.dingyi.terminal.virtualprocess.VirtualProcess;
import com.dingyi.terminal.virtualprocess.VirtualProcessSystem;

public class VirtualProcessConsoleMetaData implements  ConsoleMetaData {

    private final VirtualProcess virtualProcess;

    public VirtualProcessConsoleMetaData() {
        this.virtualProcess = VirtualProcessSystem.currentProcess();
    }

    @Override
    public boolean isStdOut() {
        return false;
    }

    @Override
    public boolean isStdErr() {
        return false;
    }

    @Override
    public int getCols() {
        return virtualProcess.getProcessEnvironment().getTermiosSupport().getColumn();
    }

    @Override
    public int getRows() {
        return virtualProcess.getProcessEnvironment().getTermiosSupport().getRow();
    }

    @Override
    public boolean isWrapStreams() {
        return false;
    }
}
