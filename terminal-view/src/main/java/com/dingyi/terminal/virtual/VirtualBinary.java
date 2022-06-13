package com.dingyi.terminal.virtual;

/**
 * A virtual binary to used for virtual process.
 */
public abstract class VirtualBinary {

    public final VirtualProcessChannel mProcessChannel;

    public VirtualBinary(VirtualProcessChannel processChannel) {
        mProcessChannel = processChannel;
    }



    protected abstract int start(String[] args) throws Exception;

}
