package com.dingyi.terminal.virtual;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class VirtualBinarySystem {


    private static VirtualBinarySystem INSTANCE;

    private VirtualBinarySystem() {

    }


    public static VirtualBinarySystem getInstance() {
        synchronized (VirtualBinarySystem.class) {
            if (INSTANCE == null) {
                INSTANCE = new VirtualBinarySystem();
            }
        }
        return INSTANCE;
    }


    private Map<String,Class<?>> mBinaryClasses = new HashMap<String,Class<?>>();

    public void registerBinary(String name, Class<?> clazz) {
        mBinaryClasses.put(name, clazz);
    }

    public VirtualBinary createBinary(String name, VirtualProcessChannel processChannel) {
        Class<?> clazz = mBinaryClasses.get(name);
        if (clazz == null) {
            clazz = tryLoadBinaryForResources(name);
            if (clazz == null) {
                return null;
            }
        }
        mBinaryClasses.putIfAbsent(name, clazz);
        try {
            return (VirtualBinary) clazz.getConstructor(VirtualProcessChannel.class)
                    .newInstance(processChannel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Class<?> tryLoadBinaryForResources(String name) {
        String path = "META-INF/shell-lib/" + name + ".implement.properties";
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
