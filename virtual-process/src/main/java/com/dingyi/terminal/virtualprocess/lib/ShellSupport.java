package com.dingyi.terminal.virtualprocess.lib;

import com.dingyi.terminal.virtualprocess.VirtualExecutable;
import com.dingyi.terminal.virtualprocess.VirtualProcessEnvironment;

public class ShellSupport extends VirtualExecutable {


    public ShellSupport(VirtualProcessEnvironment processChannel) {
        super(processChannel);
    }

    @Override
    protected int start(String[] args) throws Exception {

        while (true) {
            mProcessEnvironment
                    .print("$ MyShell> ");
            String line = mProcessEnvironment.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }

        return 0;
    }
}
