package com.dingyi.terminal.virtualprocess;

import com.dingyi.terminal.virtualprocess.VirtualProcessEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class VirtualTerminalEnvironment {

    final PipedInputStream terminalInputStream;
    final PipedOutputStream terminalOutputStream;
    final PipedInputStream terminalErrorStream;

    VirtualProcessEnvironment processChannel;

    public VirtualTerminalEnvironment() throws IOException {
        terminalInputStream = new PipedInputStream();
        terminalOutputStream = new PipedOutputStream();
        terminalErrorStream = new PipedInputStream();


        PipedInputStream processInputStream = new PipedInputStream();
        PipedOutputStream processOutputStream = new PipedOutputStream();
        PipedOutputStream processErrorStream = new PipedOutputStream();

        processInputStream.connect(terminalOutputStream);
        processOutputStream.connect(terminalInputStream);
        processErrorStream.connect(terminalErrorStream);

        processChannel = new VirtualProcessEnvironment(
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

    public VirtualProcessEnvironment getProcessEnvironment() {
        return processChannel;
    }
}
