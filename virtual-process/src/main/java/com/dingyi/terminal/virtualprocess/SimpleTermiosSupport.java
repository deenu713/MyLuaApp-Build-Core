package com.dingyi.terminal.virtualprocess;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//TODO:Termios Support
public class SimpleTermiosSupport {


    private final VirtualTerminalEnvironment terminalEnvironment;
    private final VirtualProcessEnvironment processEnvironment;
    //The column and row are only used for the terminal.
    private int column;

    private int row;

    public final TermiosStruct termiosStruct;

    public SimpleTermiosSupport(
            VirtualTerminalEnvironment terminalEnvironment,
            VirtualProcessEnvironment processEnvironment) {
        this.terminalEnvironment = terminalEnvironment;
        this.processEnvironment = processEnvironment;
        this.termiosStruct = TermiosStruct.DEFAULT.copy();

    }

    void doWrapper() {

        processEnvironment
                .processOutputStream = new ProcessTermiosOutputStream(processEnvironment.processOutputStream);

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


    class ProcessTermiosOutputStream extends FilterOutputStream {

        public ProcessTermiosOutputStream(OutputStream out) {
            super(out);
            buffer = new ByteArrayBuffer(1024);
        }

        private ByteArrayBuffer buffer;

        private boolean check_OPOST() {
            return TermiosStruct.isSet(termiosStruct.c_oflag, TermiosStruct.OPOST);
        }


        private void checkByte(int b) throws IOException {
            boolean needFlush = false;
            if (b == TermiosStruct.CR) {
                if (TermiosStruct.isSet(termiosStruct.c_oflag, TermiosStruct.ONLCR)) {
                    buffer.append(b);
                    buffer.append(TermiosStruct.LF);
                } else if (TermiosStruct.isSet(termiosStruct.c_oflag, TermiosStruct.ONLRET)) {
                    buffer.append(0);
                } else {
                    buffer.append(b);
                }
                needFlush = true;
            } else if (b == TermiosStruct.LF) {
                if (TermiosStruct.isSet(termiosStruct.c_oflag, TermiosStruct.OCRNL)) {
                    buffer.append(TermiosStruct.CR);
                } else {
                    buffer.append(b);
                }
            } else {
                buffer.append(b);
            }

            if (needFlush || buffer.isFull()) {
                flush();
            }

        }

        @Override
        public void write(int b) throws IOException {
            if (check_OPOST()) {
                checkByte(b);
            } else {
                out.write(b);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (check_OPOST()) {
                for (int i = off; i < off + len; i++) {
                    checkByte(b[i]);
                }
            } else {
                out.write(b, off, len);
            }
        }

        @Override
        public void flush() throws IOException {
            if (buffer.isEmpty()) {
                return;
            }
            byte[] b = buffer.toByteArray();
            out.write(b, 0, buffer.length());
            /* out.flush();*/
            buffer.clear();

        }
    }

    public class TermiosInputStream extends FilterInputStream {
        protected TermiosInputStream(InputStream in) {
            super(in);
        }
    }

    //TODO: add c_cc characters, but i don't want to support it
    static class TermiosStruct {
        public int c_iflag;
        public int c_oflag;
        public int c_lflag;
        public int c_line;
        /*  public byte[] c_cc = new byte[32];*/

        /*
         *//* c_cc characters *//*
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
        -public static final int VEOL2 = 16;*//*


        /* c_iflag bits */
        public static final int IGNBRK = 0x00000001;
        public static final int BRKINT = 0x00000002;
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
        public static final int ONLCR = 0x00000004;
        public static final int OCRNL = 0x00000008;
        public static final int ONLRET = 0x00000020;

        /* c_lflag bits */
        public static final int ISIG = 0x00000001;
        public static final int ICANON = 0x00000002;
        public static final int XCASE = 0x00000004;
        public static final int ECHO = 0x00000008;
        public static final int ECHOE = 0x00000010;


        public static final byte CR = '\n';
        public static final byte LF = '\r';
        public static final byte BACKSPACE = '\b';
        public static final byte TAB = '\t';
        public static final byte ESC = '\033';
        public static final byte SPACE = ' ';
        public static final byte DEL = 0x7F;


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


        public static final TermiosStruct DEFAULT;

        static {
            DEFAULT = new TermiosStruct();
            DEFAULT.c_iflag = ICRNL | IXON;
            DEFAULT.c_oflag = OPOST | ONLCR;
            DEFAULT.c_lflag = ISIG | ICANON | ECHO;
            DEFAULT.c_line = 0;
          /*  DEFAULT.c_cc = new byte[32];
            DEFAULT.c_cc[VINTR] = (byte) '\003';
            DEFAULT.c_cc[VQUIT] = (byte) '\034';
            DEFAULT.c_cc[VERASE] = (byte) '\177';
            DEFAULT.c_cc[VKILL] = (byte) '\025';
            DEFAULT.c_cc[VEOF] = (byte) '\004';
            DEFAULT.c_cc[VTIME] = (byte) '\0';
            DEFAULT.c_cc[VMIN] = (byte) '\1';
            DEFAULT.c_cc[VSWTC] = (byte) '\0';
            DEFAULT.c_cc[VSTART] = (byte) '\021';
            DEFAULT.c_cc[VSTOP] = (byte) '\023';
            DEFAULT.c_cc[VSUSP] = (byte) '\032';
            DEFAULT.c_cc[VEOL] = (byte) '\0';
            DEFAULT.c_cc[VREPRINT] = (byte) '\022';
            DEFAULT.c_cc[VDISCARD] = (byte) '\017';
            DEFAULT.c_cc[VWERASE] = (byte) '\027';
            DEFAULT.c_cc[VLNEXT] = (byte) '\026';
            DEFAULT.c_cc[VEOL2] = (byte) '\0';*/

        }


        public TermiosStruct copy() {
            TermiosStruct copy = new TermiosStruct();
            copy.c_iflag = c_iflag;
            copy.c_oflag = c_oflag;
            copy.c_lflag = c_lflag;
            copy.c_line = c_line;
            /* copy.c_cc = c_cc.clone();*/
            return copy;
        }
    }

}
