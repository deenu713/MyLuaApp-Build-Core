package com.dingyi.terminal.virtualprocess;

import java.io.FilterOutputStream;
import java.io.OutputStream;

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
        processOutputStream = new TerminalTermiosOutputStream(processOutputStream);
        terminalOutputStream = new ProcessTermiosOutputStream(terminalOutputStream);
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

    public static class TerminalTermiosOutputStream extends FilterOutputStream {

        public TerminalTermiosOutputStream(OutputStream out) {
            super(out);
        }
    }

    public static class ProcessTermiosOutputStream extends FilterOutputStream {

        public ProcessTermiosOutputStream(OutputStream out) {
            super(out);
        }
    }
}
