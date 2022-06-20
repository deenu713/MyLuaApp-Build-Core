/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.classloader;


import com.dingyi.groovy.android.AppDataDirGuesser;
import com.dingyi.groovy.android.compiler.DexCompiler;

import org.gradle.internal.Cast;
import org.gradle.internal.Factory;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.hash.DefaultFileHasher;
import org.gradle.internal.hash.DefaultStreamHasher;
import org.gradle.internal.hash.FileHasher;
import org.gradle.internal.hash.HashCode;
import org.gradle.internal.os.OperatingSystem;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;


//dingyi modify: support in android
public class VisitableURLClassLoader extends URLClassLoader implements ClassLoaderHierarchy {
    static {
        try {
            ClassLoader.registerAsParallelCapable();
        } catch (NoSuchMethodError ignore) {
            // Not supported on Java 6
        }
    }


    private DexClassLoader dexClassLoader;

    private FileHasher hasher;


    private final Map<Object, Object> userData = new HashMap<>();

    /**
     * This method can be used to store user data that should live among with this classloader
     *
     * @param consumerId the consumer
     * @param onMiss     called to create the initial data, when not found
     * @param <T>        the type of data
     * @return user data
     */
    public synchronized <T> T getUserData(Object consumerId, Factory<T> onMiss) {
        if (userData.containsKey(consumerId)) {
            return Cast.uncheckedCast(userData.get(consumerId));
        }
        T value = onMiss.create();
        userData.put(consumerId, value);
        return value;
    }

    // TODO:lptr When we drop Java 8 support we can switch to using ClassLoader.getName() instead of storing our own
    private final String name;

    public VisitableURLClassLoader(String name, ClassLoader parent, Collection<URL> urls) {
        this(name, urls.toArray(new URL[0]), parent);
    }

    public VisitableURLClassLoader(String name, ClassLoader parent, ClassPath classPath) {
        this(name, classPath.getAsURLArray(), parent);
    }

    private VisitableURLClassLoader(String name, URL[] classpath, ClassLoader parent) {
        super(classpath, parent);
        this.name = name;

        if (OperatingSystem.current().isAndroid()) {
            hasher = new DefaultFileHasher(new DefaultStreamHasher());
            addDex(classpath);
        }

    }


    public void addDex(URL[] classpath) {
        for (URL url : classpath) {
            addDex(url);
        }
    }

    public void addDex(URL url) {
        try {
            tryLoadDexFile(new File(url.toURI()), this);
        } catch (Exception e) {
            compileAndLoadDex(url);
        }
    }


    private void tryLoadDexFile(File url, ClassLoader parent) {
        //try load dex file
        try {
            //check if dex file is valid
            new DexFile(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (dexClassLoader == null) {
            dexClassLoader = new DexClassLoader(url.getAbsolutePath(), null, null, parent);
        } else {
            Method addDexPathMethod = null;
            try {
                addDexPathMethod = BaseDexClassLoader.class.getDeclaredMethod("addDexPath", String.class);
                addDexPathMethod.setAccessible(true);

                addDexPathMethod.invoke(dexClassLoader, url.getAbsolutePath());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private void compileAndLoadDex(URL url) {
        File compileDir = new File(new AppDataDirGuesser().guess(), "dexCache");
        if (!compileDir.exists()) {
            compileDir.mkdirs();
        }
        File compileFile = new File(url.getFile());
        HashCode hashCode = hasher.hash(new File(url.getFile()));
        File dexFile = new File(compileDir, "Generated_" + hashCode + ".jar");
        if (dexFile.exists()) {
            tryLoadDexFile(dexFile, getParent());
            return;
        }
        DexCompiler.INSTANCE.compileClassFile(List.of(compileFile), dexFile);
        tryLoadDexFile(dexFile, getParent());
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (dexClassLoader != null) {
            try {
                Method findClassMethod = ClassLoader.class.getDeclaredMethod("findClass", String.class);
                findClassMethod.setAccessible(true);
                Class<?> clazz = (Class<?>) findClassMethod.invoke(dexClassLoader, name);
                if (clazz != null) {
                    return clazz;
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return super.findClass(name);
    }

    @Override
    protected Class<?> findClass(String moduleName, String name) {
        return super.findClass(moduleName, name);
    }

    @Override
    protected URL findResource(String moduleName, String name) throws IOException {
        return super.findResource(moduleName, name);
    }

    @Override
    public URL findResource(String name) {
        if (dexClassLoader != null) {
            try {
                Method findResourceMethod = ClassLoader.class.getDeclaredMethod("findResource", String.class);
                findResourceMethod.setAccessible(true);
                URL url = (URL) findResourceMethod.invoke(dexClassLoader, name);
                if (url != null) {
                    return url;
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return super.findResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        if (dexClassLoader != null) {
            try {
                Method findResourcesMethod = ClassLoader.class.getDeclaredMethod("findResources", String.class);
                findResourcesMethod.setAccessible(true);
                Enumeration<URL> urls = (Enumeration<URL>) findResourcesMethod.invoke(dexClassLoader, name);
                if (urls != null) {
                    return urls;
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return super.findResources(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
        if (OperatingSystem.current().isAndroid()) {
            addDex(url);
        }
    }


    @Override
    public String toString() {
        return VisitableURLClassLoader.class.getSimpleName() + "(" + name + ")";
    }


    @Override
    public void visit(ClassLoaderVisitor visitor) {
        URL[] urls = getURLs();
        visitor.visitSpec(new Spec(name, Arrays.asList(urls)));
        visitor.visitClassPath(urls);
        visitor.visitParent(getParent());
    }

    public static class Spec extends ClassLoaderSpec {
        final String name;
        final List<URL> classpath;

        public String getName() {
            return name;
        }

        public Spec(String name, List<URL> classpath) {
            this.name = name;
            this.classpath = classpath;
        }

        public List<URL> getClasspath() {
            return classpath;
        }

        @Override
        public String toString() {
            return "{url-class-loader name:" + name + ", classpath:" + classpath + "}";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            Spec other = (Spec) obj;
            return classpath.equals(other.classpath);
        }

        @Override
        public int hashCode() {
            return classpath.hashCode();
        }
    }
}
