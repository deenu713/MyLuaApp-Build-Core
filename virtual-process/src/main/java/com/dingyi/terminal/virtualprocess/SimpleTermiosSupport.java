package com.dingyi.terminal.virtualprocess;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

//TODO:Termios Support
public class SimpleTermiosSupport {

    private OutputStream processOutputStream;
    private InputStream processInputStream;

    private int column;

    private int row;

    public final TermiosStruct termiosStruct;

    public SimpleTermiosSupport(
            OutputStream processOutputStream,
            InputStream processInputStream
    ) {
        this.processOutputStream = processOutputStream;
        this.processInputStream = processInputStream;
        this.termiosStruct = new TermiosStruct();
    }

    void doWrapper() {
        processOutputStream = new TermiosOutputStream(processOutputStream);
        processInputStream = new TermiosInputStream(processInputStream);
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

    public InputStream getProcessInputStream() {
        return processInputStream;
    }

    public class TermiosOutputStream extends FilterOutputStream {

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
            }/* else if (b == '\r') {
                tmpBuffer[0] = '\r';
                tmpBuffer[1] = '\n';
                out.write(tmpBuffer, 0, 2);
            } */ else {
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

    public class TermiosInputStream extends FilterInputStream {

        protected TermiosInputStream(InputStream in) {
            super(in);
        }
    }

    static class TermiosStruct {
        public int c_iflag;
        public int c_oflag;
        public int c_lflag;
        public int c_line;
        public byte[] c_cc = new byte[32];


        /* c_cc characters */
        public static final int VINTR = 0;
        public static final int VQUIT = 1;
        public static final int VERASE = 2;
        public static final int VKILL = 3;
        public static final int VEOF = 4;
        public static final int VTIME = 5;
        public static final int VMIN = 6;
        public static final int VSWTC = 7;
        public static final int VSTART = 8;
        public static final int VSTOP = 9;
        public static final int VSUSP = 10;
        public static final int VEOL = 11;
        public static final int VREPRINT = 12;
        public static final int VDISCARD = 13;
        public static final int VWERASE = 14;
        public static final int VLNEXT = 15;
        public static final int VEOL2 = 16;


        /* c_iflag bits */
        public static final int IGNBRK = 0x00000001;
        public static final int BRKINT = 0x00000002;
        public static final int IGNPAR = 0x00000004;
        public static final int PARMRK = 0x00000008;
        public static final int INPCK = 0x00000010;
        public static final int ISTRIP = 0x00000020;
        public static final int INLCR = 0x00000040;
        public static final int IGNCR = 0x00000080;
        public static final int ICRNL = 0x00000100;
        public static final int IXON = 0x00000200;
        public static final int IXOFF = 0x00000400;
        public static final int IXANY = 0x00000800;
        public static final int IMAXBEL = 0x00001000;

        /* c_oflag bits */
        public static final int OPOST = 0x00000001;
        public static final int OLCUC = 0x00000002;
        public static final int ONLCR = 0x00000004;
        public static final int OCRNL = 0x00000008;
        public static final int ONOCR = 0x00000010;
        public static final int ONLRET = 0x00000020;
        public static final int OFILL = 0x00000040;
        public static final int OFDEL = 0x00000080;
        public static final int VTDLY = 0x00000100;
        public static final int VT0 = 0x00000200;
        public static final int VT1 = 0x00000400;

        /* c_lflag bits */
        public static final int ISIG = 0x00000001;
        public static final int ICANON = 0x00000002;
        public static final int XCASE = 0x00000004;
        public static final int ECHO = 0x00000008;
        public static final int ECHOE = 0x00000010;
        public static final int ECHOK = 0x00000020;
        public static final int ECHONL = 0x00000040;
        public static final int NOFLSH = 0x00000080;
        public static final int TOSTOP = 0x00000100;
        public static final int ECHOCTL = 0x00000200;
        public static final int ECHOPRT = 0x00000400;
        public static final int ECHOKE = 0x00000800;
        public static final int FLUSHO = 0x00001000;
        public static final int PENDIN = 0x00002000;
        public static final int IEXTEN = 0x00004000;


        public static boolean isSet(int flag, int mask) {
            return (flag & mask) == mask;
        }

        public static int set(int flag, int mask) {
            return flag | mask;
        }

        public static int unset(int flag, int mask) {
            return flag & ~mask;
        }

        public static int set(int flag, int... masks) {
            for (int mask : masks) {
                flag = set(flag, mask);
            }
            return flag;
        }

        public static int unset(int flag, int... masks) {
            for (int mask : masks) {
                flag = unset(flag, mask);
            }
            return flag;
        }


    }

}
