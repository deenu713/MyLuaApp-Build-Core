package com.dingyi.terminal.virtual;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

class VirtualExecutableExecutor implements Runnable {

    private final VirtualExecutable mBinary;
    private final VirtualProcess mProcess;

    final CountDownLatch latch = new CountDownLatch(1);

    VirtualExecutableExecutor(VirtualExecutable binary, VirtualProcess process) {
        mBinary = binary;
        mProcess = process;
    }


    @Override
    public void run() {
        int ret;

        try {
            ret = mBinary.start(mProcess.args);
        } catch (Exception e) {
            ret = -1;
            try {
                mProcess
                        .getProcessChannel()
                        .processErrorStream
                        .write(dumpException(e));
            } catch (IOException ex) {
                // ignore
            }
        }

        mProcess.getProcessChannel()
                .exit(ret);
        try {
            mProcess.getProcessChannel()
                    .destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

        latch.countDown();


    }



    private byte[] dumpException(Exception e) {
        return e.getMessage().getBytes();
    }

    public void interrupt() {
        Thread.currentThread().interrupt();
    }
}
