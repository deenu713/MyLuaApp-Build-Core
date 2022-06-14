import com.dingyi.terminal.virtualprocess.VirtualExecutable;
import com.dingyi.terminal.virtualprocess.VirtualExecutableExecutorPool;
import com.dingyi.terminal.virtualprocess.VirtualExecutableSystem;
import com.dingyi.terminal.virtualprocess.VirtualProcess;
import com.dingyi.terminal.virtualprocess.VirtualProcessEnvironment;
import com.dingyi.terminal.virtualprocess.VirtualProcessSystem;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestFile {

    @Test
    public void test() {
        VirtualExecutableSystem.getInstance()
                .registerBinary("echo", echo.class);

        VirtualProcess process= VirtualProcessSystem
                .createProcess("echo", "Hello", "VirtualProcess");

        process.start();
        try {
            VirtualExecutableExecutorPool
                    .getInstance()
                    .waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class echo extends VirtualExecutable {

        public echo(VirtualProcessEnvironment currentProcess) {
            super(currentProcess);
        }

        @Override
        protected int start(String[] args) throws IOException {
            for (String a : args) {
                mProcessEnvironment
                        .write(a.getBytes(StandardCharsets.UTF_8));

                mProcessEnvironment
                        .write(" ".getBytes(StandardCharsets.UTF_8));
            }
            mProcessEnvironment
                    .write("\n".getBytes(StandardCharsets.UTF_8));
            return 0;
        }
    }
}
