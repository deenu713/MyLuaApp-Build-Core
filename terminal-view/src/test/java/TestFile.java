import com.dingyi.terminal.virtual.VirtualBinary;
import com.dingyi.terminal.virtual.VirtualBinaryExecutorPool;
import com.dingyi.terminal.virtual.VirtualBinarySystem;
import com.dingyi.terminal.virtual.VirtualProcess;
import com.dingyi.terminal.virtual.VirtualProcessChannel;
import com.dingyi.terminal.virtual.VirtualProcessSystem;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestFile {

    @Test
    public void test() {
        VirtualBinarySystem.getInstance()
                .registerBinary("echo", echo.class);

        VirtualProcess process= VirtualProcessSystem
                .createProcess("echo", "Hello", "VirtualProcess");

        process.start();
        try {
            VirtualBinaryExecutorPool
                    .getInstance()
                    .waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class echo extends VirtualBinary {

        public echo(VirtualProcessChannel currentProcess) {
            super(currentProcess);
        }

        @Override
        protected int start(String[] args) throws IOException {
            for (String a : args) {
                mProcessChannel
                        .write(a.getBytes(StandardCharsets.UTF_8));

                mProcessChannel
                        .write(" ".getBytes(StandardCharsets.UTF_8));
            }
            mProcessChannel
                    .write("\n".getBytes(StandardCharsets.UTF_8));
            return 0;
        }
    }
}
