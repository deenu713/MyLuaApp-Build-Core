package com.dingyi.terminal.virtual;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VirtualProcessSystem {

    private static VirtualProcessSystem INSTANCE;

    private VirtualProcessSystem() {
    }

    public static VirtualProcessSystem getInstance() {
        synchronized (VirtualProcessSystem.class) {
            if (INSTANCE == null) {
                INSTANCE = new VirtualProcessSystem();
            }
        }
        return INSTANCE;
    }


    private Map<Integer, VirtualProcess> mProcesses = new HashMap<Integer, VirtualProcess>();

    private int currentProcessId = 0;

    private int generateProcessId() {
        return currentProcessId++;
    }

    public static VirtualProcess createProcess(String cmd) {
        return getInstance().createProcessFor(new VirtualProcess(cmd));
    }


    public static VirtualProcess createProcess(String cmd, String... args) {
        return getInstance().createProcessFor(new VirtualProcess(cmd, args));
    }

    public static VirtualProcess createProcess(String cmd,
                                               String cwd,
                                               String[] args,
                                               String[] envVars) {
        return getInstance().createProcessFor(new VirtualProcess(cmd, cwd, args, envVars));
    }

    public VirtualProcess createProcessFor(VirtualProcess process) {
        process.setProcessId(generateProcessId());
        mProcesses.put(process.getProcessId(), process);
        new Thread("VirtualProcess-Listener") {
            @Override
            public void run() {
                while (true) {
                    if (!process.isStart) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    remove(process);
                }
                remove(process);
            }


            void remove(VirtualProcess process) {
                mProcesses.remove(process.getProcessId());
            }
        }.start();
        return process;
    }


    public static VirtualProcess getProcess(int processId) {
        return getInstance().getProcessFor(processId);
    }

    public VirtualProcess getProcessFor(int processId) {
        return mProcesses.get(processId);
    }

    public static void waitFor(int processId) {
        try {
            getInstance().waitForImpl(processId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void waitForImpl(int processId) throws InterruptedException {
        VirtualProcess process = getProcessFor(processId);
        if (process != null) {
            process.waitFor();
        }
    }

    public static boolean killProcess(int processId) {
        return getInstance().killProcessFor(processId);
    }

    public boolean killProcessFor(int processId) {
        VirtualProcess process = mProcesses.get(processId);
        try {
            process.destroy();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}


