package org.gradle.internal.nativeintegration.processenvironment;

import android.os.Process;

import com.dingyi.terminal.virtualprocess.VirtualProcess;
import com.dingyi.terminal.virtualprocess.VirtualProcessSystem;

import org.gradle.internal.nativeintegration.NativeIntegrationException;

import java.io.File;

public class VirtualProcessEnvironment extends AbstractProcessEnvironment {


    private File processDir;

    @Override
    public File getProcessDir() throws NativeIntegrationException {
        if (processDir == null) {
            processDir = new File(VirtualProcessSystem.currentProcess().getProcessEnvironment()
                    .getCurrentWorkDir());
        }
        return processDir;
    }

    @Override
    public Long getPid() throws NativeIntegrationException {
        return (long)VirtualProcessSystem
                .currentProcess()
                .getProcessId();
    }

    @Override
    public void detachProcess() {
        //do nothing
    }

    @Override
    protected void removeNativeEnvironmentVariable(String name) {
        VirtualProcessSystem
                .currentProcess()
                .getProcessEnvironment()
                .removeEnvironment(name);
    }

    @Override
    protected void setNativeEnvironmentVariable(String name, String value) {
        VirtualProcessSystem
                .currentProcess()
                .getProcessEnvironment()
                .putEnvironment(name,value);
    }

    @Override
    protected void setNativeProcessDir(File processDir) {
        VirtualProcessSystem
                .currentProcess()
                .getProcessEnvironment()
                .setCurrentWorkDir(processDir.getAbsolutePath());
    }
}
