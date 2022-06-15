package com.dingyi.terminal.virtualprocess;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

//TODO:Termios Support
public class SimpleTermiosSupport {

    private OutputStream processOutputStream;
    private OutputStream terminalOutputStream;

    private int column;

    private int row;

    public SimpleTermiosSupport(
            OutputStream processOutputStream,
            OutputStream terminalOutputStream
    ) {
        this.processOutputStream = processOutputStream;
        this.terminalOutputStream = terminalOutputStream;
    }

    void doWrapper() {
        processOutputStream = new TermiosOutputStream(processOutputStream);
        terminalOutputStream = new TermiosOutputStream(terminalOutputStream);
    }


    public synchronized void setColumn(int column) {
        synchronized (this) {
            this.column = column;
        }
    }

    public synchronized void setSize(int column, int row) {
        synchronized (this) {
            this.row = row;
            this.column = column;
        }
    }

    public synchronized void setRow(int row) {
        synchronized (this) {
            this.row = row;
        }
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public OutputStream getProcessOutputStream() {
        return processOutputStream;
    }

    public OutputStream getTerminalOutputStream() {
        return terminalOutputStream;
    }

    public static class TermiosOutputStream extends FilterOutputStream {

        public TermiosOutputStream(OutputStream out) {
            super(out);
        }

        private byte[] tmpBuffer = new byte[1024];


        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }


        private void writeTranslate(byte b) throws IOException {
            if (b == '\n') {
                tmpBuffer[0] = '\r';
                tmpBuffer[1] = '\n';
                out.write(tmpBuffer, 0, 2);
            } else if (b == '\r') {
                tmpBuffer[0] = '\r';
                tmpBuffer[1] = '\n';
                out.write(tmpBuffer, 0, 2);
            } else {
                tmpBuffer[0] = b;
                out.write(tmpBuffer, 0, 1);
            }

        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                writeTranslate(b[i]);
            }
        }

        @Override
        public void write(int b) throws IOException {
            tmpBuffer[0] = (byte) b;
            write(tmpBuffer, 0, 1);
        }


        @Override
        public void flush() throws IOException {
            super.flush();
        }
    }


}
