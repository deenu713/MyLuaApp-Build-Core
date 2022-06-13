package com.dingyi.terminal.virtual;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

class VirtualBinaryExecutor implements Runnable {

    private final VirtualBinary mBinary;
    private final VirtualProcess mProcess;

    final CountDownLatch latch = new CountDownLatch(1);

    VirtualBinaryExecutor(VirtualBinary binary, VirtualProcess process) {
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

        latch.countDown();


    }



    private byte[] dumpException(Exception e) {
        return e.getMessage().getBytes();
    }

    public void interrupt() {
        Thread.currentThread().interrupt();
    }
}
