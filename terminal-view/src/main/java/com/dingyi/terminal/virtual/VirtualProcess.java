package com.dingyi.terminal.virtual;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A virtual process is a process that is not actually running in the system.
 * It uses for terminal emulator.
 * Not support termios and pty set.
 */
public class VirtualProcess {


    private Map<String, String> env = new HashMap<String, String>();

    final String cwd;
    final String cmd;
    final String[] args;



     boolean isStart = false;

    private VirtualProcessChannel processChannel;

    private VirtualExecutableExecutor binaryExecutor;

    private static final String[] NULL_ARRAY = new String[0];

    private int processId;

    VirtualProcess(String cmd, String cwd, String[] args, String[] envVars) {
        this.cmd = cmd;
        this.cwd = cwd;
        this.args = args;
        parseEnv(envVars);
    }

    VirtualProcess(String cmd) {
        this(cmd, NULL_ARRAY);
    }

    VirtualProcess(String cmd, String[] args, String cwd) {
        this(cmd, cwd, args, NULL_ARRAY);
    }

    VirtualProcess(String cmd, String[] args) {
        this(cmd, args, new File("").getAbsoluteFile().getAbsolutePath());
    }


    void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getProcessId() {
        return processId;
    }

    private static String[] transformMapToArray(Map<String, String> map) {
        String[] array = new String[map.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            array[i++] = entry.getKey() + "=" + entry.getValue();
        }
        return array;
    }

    private void parseEnv(String[] envVars) {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.environment().putAll(env);

        for (String envVar : envVars) {
            String[] kv = envVar.split("=");
            env.put(kv[0], kv[1]);
        }
    }


    public VirtualProcessChannel getProcessChannel() {
        return processChannel;
    }



    /**
     * Return the process is alive or not.
     *
     * @return
     */
    public boolean isAlive() {
        return processChannel.exitValue() == null;
    }

    public void waitFor() throws InterruptedException {
        while (binaryExecutor == null) {
            Thread.sleep(1000);
        }
        binaryExecutor.latch.await();
    }

    public void destroy() throws IOException {
        if (processChannel != null) {
            processChannel.destroy();
        }
    }

    public void killProcess() {
        binaryExecutor.interrupt();
    }

    public void setProcessChannel(VirtualProcessChannel processChannel) {
        if (isStart) {
            return;
        }
        this.processChannel = processChannel;
    }

    public int exitValue() {
        return processChannel.exitValue();
    }

    public void start() {
        if (processChannel == null) {
            processChannel = new VirtualProcessChannel();
        }
        processChannel
                .cwd = cwd;
        processChannel.env = env;
        processChannel.args = args;
        VirtualExecutable binary = VirtualExecutableSystem.getInstance().createBinary(cmd, processChannel);
        if (binary == null) {
            throw new RuntimeException("Can't find binary");
        }
        binaryExecutor = new VirtualExecutableExecutor(
                binary, this
        );
        VirtualExecutableExecutorPool
                .getInstance()
                .execBinaryExecutor(binaryExecutor);
        isStart = true;
    }

}
