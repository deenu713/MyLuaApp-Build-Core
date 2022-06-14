package com.dingyi.terminal.virtual;

import android.icu.util.Output;

import com.dingyi.terminal.ByteQueue;


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


    public String[] args;

    public String cwd;

    Map<String, String> env = new HashMap<>();

    Integer exitValue = null;

    public VirtualProcessChannel(InputStream processInputStream, OutputStream processOutputStream, OutputStream processErrorStream) {
        this.processInputStream = processInputStream;
        this.processOutputStream = processOutputStream;
        this.processErrorStream = processErrorStream;




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
        processOutputStream.flush();
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

