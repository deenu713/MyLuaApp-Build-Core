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


import com.dingyi.groovy.android.compiler.DexCompiler;

import org.gradle.internal.Cast;
import org.gradle.internal.Factory;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.os.OperatingSystem;


import java.io.File;
import java.io.IOException;
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


//dingyi modify: support load class file in android
public class VisitableURLClassLoader extends URLClassLoader implements ClassLoaderHierarchy {
    static {
        try {
            ClassLoader.registerAsParallelCapable();
        } catch (NoSuchMethodError ignore) {
            // Not supported on Java 6
        }
    }

    private List<ClassLoader> allDexLoader;

    private final Map<Object, Object> userData = new HashMap<Object, Object>();

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
            allDexLoader = new ArrayList<>();
            loadDexForClassPath(classpath);
        }

    }


    ClassLoader loadDexFromFile(ClassLoader classLoader, URL[] urls) {
        List<File> classFiles = new ArrayList<>();
        for (URL url : urls) {
            classFiles.add(new File(url.getFile()));
        }

        return DexCompiler.INSTANCE
                .compileAndLoadClassFiles(classFiles, classLoader);
    }

    private void loadDexForClassPath(URL[] classPath) {

        if (classPath.length == 0) {
            return;
        }

        ClassLoader dexClassLoader = loadDexFromFile(getParent(), classPath);

        allDexLoader.add(dexClassLoader);


    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (OperatingSystem.current().isAndroid()) {
            for (ClassLoader classLoader : allDexLoader) {
                try {
                    Class<?> classInDex =  classLoader.loadClass(name);
                    if (classInDex != null) {
                        return classInDex;
                    }
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        return super.findClass(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (OperatingSystem.current().isAndroid()) {
            for (ClassLoader classLoader : allDexLoader) {
                try {
                    Class<?> classInDex =  classLoader.loadClass(name);
                    if (classInDex != null) {
                        return classInDex;
                    }
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        return super.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (OperatingSystem.current().isAndroid()) {
            for (ClassLoader classLoader : allDexLoader) {
                try {
                    Class<?> classInDex =  classLoader.loadClass(name);
                    if (classInDex != null) {
                        return classInDex;
                    }
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String moduleName, String name) {
        if (OperatingSystem.current().isAndroid()) {
            for (ClassLoader classLoader : allDexLoader) {
                try {
                    Class<?> classInDex =  classLoader.loadClass(name);
                    if (classInDex != null) {
                        return classInDex;
                    }
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        return super.findClass(moduleName, name);
    }

    @Override
    protected URL findResource(String moduleName, String name) throws IOException {
        if (OperatingSystem.current().isAndroid()) {
            for (ClassLoader classLoader : allDexLoader) {
                URL url = classLoader.getResource(name);
                if (url != null) {
                    return url;
                }
            }
        }
        return super.findResource(moduleName, name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        if (OperatingSystem.current().isAndroid()) {
            for (ClassLoader classLoader : allDexLoader) {
                Enumeration<URL> urls = classLoader.getResources(name);
                if (urls.hasMoreElements()) {
                    return urls;
                }
            }
        }
        return super.findResources(name);
    }

    @Override
    public URL findResource(String name) {
        if (OperatingSystem.current().isAndroid()) {
            for (ClassLoader classLoader : allDexLoader) {
                URL url = classLoader.getResource(name);
                if (url != null) {
                    return url;
                }
            }
        }
        return super.findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (OperatingSystem.current().isAndroid()) {
            for (ClassLoader classLoader : allDexLoader) {
                Enumeration<URL> urls = classLoader.getResources(name);
                if (urls.hasMoreElements()) {
                    return urls;
                }
            }
        }
        return super.getResources(name);
    }

    @Nullable
    @Override
    public URL getResource(String name) {
        if (OperatingSystem.current().isAndroid()) {
            for (ClassLoader classLoader : allDexLoader) {
                URL url = classLoader.getResource(name);
                if (url != null) {
                    return url;
                }
            }
        }
        return super.getResource(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
        loadDexForClassPath(new URL[]{url});
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
