package com.dingyi.terminal.virtualprocess;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class VirtualExecutableService {


    private static VirtualExecutableService INSTANCE;

    private VirtualExecutableService() {
        tryLoadBinaryForResources(this.getClass().getClassLoader());
    }


    public static VirtualExecutableService getInstance() {
        synchronized (VirtualExecutableService.class) {
            if (INSTANCE == null) {
                INSTANCE = new VirtualExecutableService();
            }
        }
        return INSTANCE;
    }


    private final Map<String, Class<?>> mExecutableClasses = new HashMap<String, Class<?>>();

    public void registerExecutable(String name, Class<?> clazz) {
        mExecutableClasses.put(name, clazz);
    }

    public VirtualExecutable createExecutable(String name, VirtualProcessEnvironment processChannel) {
        Class<?> clazz = mExecutableClasses.get(name);
        if (clazz == null) {
            return null;
        }
        mExecutableClasses.putIfAbsent(name, clazz);
        try {
            return (VirtualExecutable) clazz.getConstructor(VirtualProcessEnvironment.class)
                    .newInstance(processChannel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void tryLoadBinaryForResources(ClassLoader classLoader) {
        String path = "META-INF/shell-plugins/com.dingyi.terminal.support";
        try {
            InputStream stream = classLoader
                    .getResourceAsStream(path);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(":");
                    if (split.length == 2) {
                        String name = split[0];
                        String className = split[1];
                        Class<?> clazz = Class.forName(className);
                        registerExecutable(name, clazz);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean unregisterExecutable(String name) {
        return mExecutableClasses.remove(name) != null;
    }


}
