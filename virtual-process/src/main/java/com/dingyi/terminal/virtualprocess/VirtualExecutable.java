package com.dingyi.terminal.virtualprocess;

/**
 * A virtual binary to used for virtual process.
 */
public abstract class VirtualExecutable {

    public final VirtualProcessEnvironment mProcessEnvironment;

    public VirtualExecutable(VirtualProcessEnvironment processChannel) {
        mProcessEnvironment = processChannel;

    }

    protected abstract int start(String[] args) throws Exception;

}
