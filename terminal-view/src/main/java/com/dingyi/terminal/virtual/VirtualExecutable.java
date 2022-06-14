package com.dingyi.terminal.virtual;

/**
 * A virtual binary to used for virtual process.
 */
public abstract class VirtualExecutable {

    public final VirtualProcessChannel mProcessChannel;

    public VirtualExecutable(VirtualProcessChannel processChannel) {
        mProcessChannel = processChannel;
    }



    protected abstract int start(String[] args) throws Exception;

}
