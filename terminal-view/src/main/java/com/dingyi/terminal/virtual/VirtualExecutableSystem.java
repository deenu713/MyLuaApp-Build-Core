package com.dingyi.terminal.virtual;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class VirtualExecutableSystem {


    private static VirtualExecutableSystem INSTANCE;

    private VirtualExecutableSystem() {

    }


    public static VirtualExecutableSystem getInstance() {
        synchronized (VirtualExecutableSystem.class) {
            if (INSTANCE == null) {
                INSTANCE = new VirtualExecutableSystem();
            }
        }
        return INSTANCE;
    }


    private Map<String,Class<?>> mBinaryClasses = new HashMap<String,Class<?>>();

    public void registerBinary(String name, Class<?> clazz) {
        mBinaryClasses.put(name, clazz);
    }

    public VirtualExecutable createBinary(String name, VirtualProcessChannel processChannel) {
        Class<?> clazz = mBinaryClasses.get(name);
        if (clazz == null) {
            clazz = tryLoadBinaryForResources(name);
            if (clazz == null) {
                return null;
            }
        }
        mBinaryClasses.putIfAbsent(name, clazz);
        try {
            return (VirtualExecutable) clazz.getConstructor(VirtualProcessChannel.class)
                    .newInstance(processChannel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Class<?> tryLoadBinaryForResources(String name) {
        String path = "META-INF/shell-lib/" + name + ".properties";
        try {
            InputStream stream = getClass().getClassLoader()
                    .getResourceAsStream(path);

            Properties properties = new Properties();
            properties.load(stream);
            return Class.forName(properties.getProperty("implement-class"));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean unregisterBinary(String name) {
        return mBinaryClasses.remove(name) != null;
    }


}
