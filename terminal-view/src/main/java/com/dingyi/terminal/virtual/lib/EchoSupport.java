package com.dingyi.terminal.virtual.lib;

import com.dingyi.terminal.virtual.VirtualExecutable;
import com.dingyi.terminal.virtual.VirtualProcessChannel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EchoSupport extends VirtualExecutable {
    public EchoSupport(VirtualProcessChannel currentProcess) {
        super(currentProcess);
    }

    @Override
    protected int start(String[] args) throws IOException {
        for (String a : args) {
            mProcessChannel
                    .getOutputStream()
                    .write(a.getBytes(StandardCharsets.UTF_8));

            mProcessChannel
                    .getOutputStream()
                    .write(" ".getBytes(StandardCharsets.UTF_8));
            mProcessChannel
                    .getOutputStream()
                    .flush();
        }
        mProcessChannel
                .write("\n".getBytes(StandardCharsets.UTF_8));
        return 0;
    }
}
