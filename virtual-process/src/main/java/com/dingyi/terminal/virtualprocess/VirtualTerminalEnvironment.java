package com.dingyi.terminal.virtualprocess;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VirtualTerminalEnvironment {

    final ByteQueueInputStream terminalInputStream;
    OutputStream terminalOutputStream;
    final ByteQueueInputStream terminalErrorStream;

    final ByteQueue processInputQueue;
    final ByteQueue terminalInputQueue;
    final ByteQueue terminalErrorQueue;
    VirtualProcessEnvironment processChannel;

    public VirtualTerminalEnvironment() throws IOException {

        processInputQueue = new ByteQueue(4096);
        terminalInputQueue = new ByteQueue(4096);
        terminalErrorQueue = new ByteQueue(4096);

        terminalErrorStream = new ByteQueueInputStream();

        terminalInputStream = new ByteQueueInputStream();

        ByteQueueOutputStream _terminalOutputStream = new ByteQueueOutputStream();
        terminalOutputStream = _terminalOutputStream;
        ByteQueueInputStream processInputStream = _terminalOutputStream.newQueueInputStream();
        ByteQueueOutputStream processOutputStream = terminalInputStream.newQueueOutputStream();
        ByteQueueOutputStream processErrorStream = terminalErrorStream.newQueueOutputStream();

        processChannel = new VirtualProcessEnvironment(processInputStream, processOutputStream, processErrorStream);

        SimpleTermiosSupport termiosSupport = new SimpleTermiosSupport(this, processChannel);

        termiosSupport.doWrapper();

        processChannel.termiosSupport = termiosSupport;


    }

    public InputStream getInputStream() {
        return terminalInputStream;
    }

    public OutputStream getOutputStream() {
        return terminalOutputStream;
    }

    public InputStream getErrorStream() {
        return terminalErrorStream;
    }

    public void destroy() throws IOException {
        terminalInputStream.close();
        terminalOutputStream.close();
        terminalErrorStream.close();
        processChannel.destroy();
    }

    public VirtualProcessEnvironment getProcessEnvironment() {
        return processChannel;
    }
}
