package com.dingyi.terminal.virtualprocess;

import android.graphics.BlendModeColorFilter;
import android.icu.util.Output;

import com.dingyi.terminal.virtualprocess.VirtualProcessEnvironment;
import com.dingyi.terminal.virtualprocess.stream.QueueInputStream;
import com.dingyi.terminal.virtualprocess.stream.QueueOutputStream;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class VirtualTerminalEnvironment {

    final QueueInputStream terminalInputStream;
    final QueueOutputStream terminalOutputStream;
    final QueueInputStream terminalErrorStream;

    final BlockingQueue<Integer> processInputQueue;
    final BlockingQueue<Integer> terminalInputQueue;
    final BlockingQueue<Integer> terminalErrorQueue;
    VirtualProcessEnvironment processChannel;

    public VirtualTerminalEnvironment() throws IOException {

        processInputQueue = new LinkedBlockingQueue<>();
        terminalInputQueue = new LinkedBlockingQueue<>();
        terminalErrorQueue = new LinkedBlockingQueue<>();

        terminalErrorStream = new QueueInputStream(terminalErrorQueue);

        terminalInputStream = new QueueInputStream(terminalInputQueue);

        terminalOutputStream = new QueueOutputStream(processInputQueue);

        QueueInputStream processInputStream = terminalOutputStream.newQueueInputStream();
        QueueOutputStream processOutputStream = terminalInputStream.newQueueOutputStream();
        QueueOutputStream processErrorStream = terminalErrorStream.newQueueOutputStream();

        processChannel = new VirtualProcessEnvironment(processInputStream, processOutputStream, processErrorStream);

        SimpleTermiosSupport termiosSupport = new SimpleTermiosSupport(this, processChannel);

        //termiosSupport.doWrapper();

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
