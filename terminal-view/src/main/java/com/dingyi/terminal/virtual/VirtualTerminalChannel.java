package com.dingyi.terminal.virtual;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class VirtualTerminalChannel {

    final PipedInputStream terminalInputStream;
    final PipedOutputStream terminalOutputStream;
    final PipedInputStream terminalErrorStream;

    VirtualProcessChannel processChannel;

    public VirtualTerminalChannel() throws IOException {
        terminalInputStream = new PipedInputStream();
        terminalOutputStream = new PipedOutputStream();
        terminalErrorStream = new PipedInputStream();

        PipedInputStream processInputStream = new PipedInputStream();
        PipedOutputStream processOutputStream = new PipedOutputStream();
        PipedOutputStream processErrorStream = new PipedOutputStream();

        processInputStream.connect(terminalOutputStream);
        processOutputStream.connect(terminalInputStream);
        processErrorStream.connect(terminalErrorStream);

        processChannel = new VirtualProcessChannel(
                processInputStream, processOutputStream, processErrorStream);

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

    public VirtualProcessChannel getProcessChannel() {
        return processChannel;
    }
}
