package com.dingyi.terminal.virtual;

import android.icu.util.Output;

import com.dingyi.terminal.ByteQueue;

import org.apache.commons.io.input.buffer.CircularByteBuffer;

import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class VirtualProcessChannel {

    InputStream processInputStream;
    OutputStream processOutputStream;
    OutputStream processErrorStream;

    WrapperInputStream clientInputStream;

    WrapperOutputStream clientOutputStream;

    public String[] args;

    public String cwd;

    Map<String, String> env = new HashMap<>();

    Integer exitValue = null;

    public VirtualProcessChannel(InputStream processInputStream, OutputStream processOutputStream, OutputStream processErrorStream) {
        this.processInputStream = processInputStream;
        this.processOutputStream = processOutputStream;
        this.processErrorStream = processErrorStream;


        this.clientOutputStream = new WrapperOutputStream(processOutputStream) {
            @Override
            public void write(int b) throws IOException {
                super.write(b);
                clientInputStream.lock.lock();
                clientInputStream.byteBuffer.add((byte) b);
                clientInputStream.lock.unlock();
            }

            @Override
            public void write(byte[] b) throws IOException {
                super.write(b);
                clientInputStream.lock.lock();
                clientInputStream.byteBuffer.add(b, 0, b.length);
                clientInputStream.lock.unlock();
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                super.write(b, off, len);
                clientInputStream.lock.lock();
                clientInputStream.byteBuffer.add(b, off, len);
                clientInputStream.lock.unlock();
            }
        };
        this.clientInputStream = new WrapperInputStream(processInputStream) {

            @Override
            public int read() throws IOException {
                clientOutputStream.lock.lock();
                if (clientOutputStream.byteBuffer.hasBytes()) {
                    int data = clientOutputStream.byteBuffer.read();
                    clientOutputStream.lock.unlock();
                    return data;
                }

                clientOutputStream.lock.unlock();

                return super.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                clientOutputStream.lock.lock();
                if (clientOutputStream.byteBuffer.hasBytes()) {

                    clientOutputStream.byteBuffer.read(b, 0, b.length);
                    int data = b.length;
                    clientOutputStream.lock.unlock();
                    return data;
                }
                clientOutputStream.lock.unlock();
                return super.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                clientOutputStream.lock.lock();
                if (clientOutputStream.byteBuffer.hasBytes()) {
                    int bytes = byteBuffer.getCurrentNumberOfBytes();
                    clientOutputStream.byteBuffer.read(b, off, len);
                    int data = byteBuffer.getCurrentNumberOfBytes() - bytes;
                    clientOutputStream.lock.unlock();
                    return data;
                }
                clientOutputStream.lock.unlock();
                return super.read();
            }
        };
    }


    public VirtualProcessChannel(InputStream processInputStream) {
        this(processInputStream, System.out, System.err);
    }

    public VirtualProcessChannel() {
        this(System.in);
    }


    public Map<String, String> getEnv() {
        if (env == null) {
            env = new HashMap<String, String>();
        }
        return env;
    }

    public void destroy() throws IOException {
        clientInputStream.close();
        clientOutputStream.close();
        processInputStream.close();
        processOutputStream.close();
        processErrorStream.close();
    }

    public InputStream getProcessInputStream() {
        return processInputStream;
    }

    public OutputStream getProcessOutputStream() {
        return processOutputStream;
    }

    public OutputStream getProcessErrorStream() {
        return processErrorStream;
    }

    public InputStream getClientInputStream() {
        return clientInputStream;
    }

    public OutputStream getClientOutputStream() {
        return clientOutputStream;
    }

    public void processWrite(byte[] b) throws IOException {
        processOutputStream.write(b);
        processOutputStream.flush();
    }

    void processWriteError(byte[] b) throws IOException {
        processErrorStream.write(b);
    }

    public void flush() throws IOException {
        processOutputStream.flush();
        clientOutputStream.flush();
    }

    void processRead(byte[] b) throws IOException {
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


    static class WrapperOutputStream extends FilterOutputStream {

        CircularByteBuffer byteBuffer = new CircularByteBuffer(2048);

        ByteBufferThread thread = new ByteBufferThread();

        ReentrantLock lock = new ReentrantLock();

        public WrapperOutputStream(OutputStream out) {
            super(out);
            thread.start();
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            thread.post(() -> {
                lock.lock();
                byteBuffer.add((byte) b);
                lock.unlock();
            });
        }

        @Override
        public void write(byte[] b) throws IOException {
            super.write(b);
            thread.post(() -> {
                lock.lock();
                byteBuffer.add(b, 0, b.length);
                lock.unlock();
            });
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            super.write(b, off, len);

            thread.post(() -> {
                lock.lock();
                byteBuffer.add(b, off, len);
                lock.unlock();
            });
        }

        @Override
        public void close() throws IOException {
            super.close();
            byteBuffer.clear();
            byteBuffer = null;
            thread.interrupt();
        }
    }

    static class WrapperInputStream extends FilterInputStream {


        CircularByteBuffer byteBuffer = new CircularByteBuffer(2048);

        ReentrantLock lock = new ReentrantLock();

        WrapperInputStream(InputStream in) {
            super(in);
        }


        @Override
        public int read() throws IOException {

            lock.lock();
            if (byteBuffer.hasBytes()) {
                int data = byteBuffer.read();
                lock.unlock();
                return data;
            }
            lock.unlock();

            return super.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            lock.lock();
            if (byteBuffer.hasBytes()) {
                byteBuffer.read(b, 0, b.length);
                int data = b.length;
                lock.unlock();
                return data;
            }
            lock.unlock();
            return super.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            lock.lock();
            if (byteBuffer.hasBytes()) {
                int bytes = byteBuffer.getCurrentNumberOfBytes();
                byteBuffer.read(b, off, len);
                int data = byteBuffer.getCurrentNumberOfBytes() - bytes;
                lock.unlock();
            }
            lock.unlock();
            return super.read();
        }

        @Override
        public void close() throws IOException {
            super.close();
            byteBuffer.clear();
            byteBuffer = null;
        }

    }


    static class ByteBufferThread extends Thread {

        private Queue<Runnable> queue = new LinkedList<>();

        @Override
        public void run() {
            try {
                while (isAlive() && !isInterrupted()) {
                    Runnable runnable = queue.poll();
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void post(Runnable runnable) {
            queue.add(runnable);
        }
    }

}

