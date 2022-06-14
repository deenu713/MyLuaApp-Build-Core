package com.dingyi.terminal.virtualprocess.lib;

import com.dingyi.terminal.virtualprocess.VirtualExecutable;
import com.dingyi.terminal.virtualprocess.VirtualProcessEnvironment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EchoSupport extends VirtualExecutable {
    public EchoSupport(VirtualProcessEnvironment currentProcess) {
        super(currentProcess);
    }

    @Override
    protected int start(String[] args) throws IOException {
        for (String a : args) {
            mProcessEnvironment
                    .getOutputStream()
                    .write(a.getBytes(StandardCharsets.UTF_8));

            mProcessEnvironment
                    .getOutputStream()
                    .write(" ".getBytes(StandardCharsets.UTF_8));
            mProcessEnvironment
                    .getOutputStream()
                    .flush();
        }
        mProcessEnvironment
                .write("\n".getBytes(StandardCharsets.UTF_8));
        return 0;
    }
}
