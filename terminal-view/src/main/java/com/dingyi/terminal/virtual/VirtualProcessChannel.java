package com.dingyi.terminal.virtual;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

public class VirtualProcessChannel {

    InputStream processInputStream;
    OutputStream processOutputStream;
    OutputStream processErrorStream;


    Integer exitValue = null;

    VirtualProcessChannel(InputStream processInputStream, OutputStream processOutputStream, OutputStream processErrorStream) {
        this.processInputStream = processInputStream;
        this.processOutputStream = processOutputStream;
        this.processErrorStream = processErrorStream;
    }


    VirtualProcessChannel(InputStream processInputStream) {
        this(processInputStream, new UnClosableOutputStream(System.out), new UnClosableOutputStream(System.err));
    }

    VirtualProcessChannel() {
        this(new UnCloseableInputStream(System.in));
    }

    static class UnCloseableInputStream extends FilterInputStream {
        public UnCloseableInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    static class UnClosableOutputStream extends FilterOutputStream {

        public UnClosableOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
            out.flush();
            out.close();
        }
    }

    public void destroy() throws IOException {
        processInputStream.close();
        processOutputStream.close();
        processErrorStream.close();
    }

    public InputStream getInputStream() {
        return processInputStream;
    }

    public OutputStream getOutputStream() {
        return processOutputStream;
    }

    public OutputStream getErrorStream() {
        return processErrorStream;
    }

    public void write(byte[] b) throws IOException {
        processOutputStream.write(b);
    }

    void writeError(byte[] b) throws IOException {
        processErrorStream.write(b);
    }

    public void flush() throws IOException {
        processOutputStream.flush();
    }

    void read(byte[] b) throws IOException {
        processInputStream.read(b);
    }


    /**
     * This method only set the exit value of the process.
     * And not destroy the process.
     *
     * @param value
     */
    void exit(int value) {
        exitValue = value;
    }

    public Integer exitValue() {
        return exitValue;
    }

}

