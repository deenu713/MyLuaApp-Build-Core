package com.dingyi.terminal.virtual.lib;

import com.dingyi.terminal.virtual.VirtualBinary;
import com.dingyi.terminal.virtual.VirtualProcessChannel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EchoSupport extends VirtualBinary {
    public EchoSupport(VirtualProcessChannel currentProcess) {
        super(currentProcess);
    }

    @Override
    protected int start(String[] args) throws IOException {
        for (String a : args) {
            mProcessChannel
                    .getProcessOutputStream()
                    .write(("\033[31m" + a + "\033[0m").getBytes(StandardCharsets.UTF_8));

            mProcessChannel
                    .getProcessOutputStream()
                    .write(" ".getBytes(StandardCharsets.UTF_8));
            mProcessChannel
                    .getProcessOutputStream()
                    .flush();
        }
        mProcessChannel
                .processWrite("\n".getBytes(StandardCharsets.UTF_8));
        return 0;
    }
}
