package org.gradle.internal.nativeintegration.processenvironment;

import android.os.Process;

import org.gradle.internal.nativeintegration.NativeIntegrationException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//dingyi modify: add fake process environment
public class AndroidProcessEnvironment extends AbstractProcessEnvironment {

    private File mWorkDir;

    private Map<String,Object> fakeEnv = new HashMap<>(System.getenv());

    @Override
    public File getProcessDir() throws NativeIntegrationException {
        return mWorkDir;
    }

    @Override
    public Long getPid() throws NativeIntegrationException {
        return (long) Process.myPid();
    }

    @Override
    public void detachProcess() {
        //do nothing
    }

    @Override
    protected void removeNativeEnvironmentVariable(String name) {
        fakeEnv.remove(name);
    }

    @Override
    protected void setNativeEnvironmentVariable(String name, String value) {
        fakeEnv.put(name, value);
    }

    @Override
    protected void setNativeProcessDir(File processDir) {
        mWorkDir = processDir;
    }
}
