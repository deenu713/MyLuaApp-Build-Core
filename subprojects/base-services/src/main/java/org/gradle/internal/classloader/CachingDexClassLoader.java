package org.gradle.internal.classloader;

import com.android.tools.r8.graph.M;
import com.dingyi.groovy.android.AppDataDirGuesser;
import com.dingyi.groovy.android.compiler.DexCompiler;

import org.gradle.internal.hash.DefaultFileHasher;
import org.gradle.internal.hash.DefaultStreamHasher;
import org.gradle.internal.hash.FileHasher;
import org.gradle.internal.hash.HashCode;
import org.gradle.internal.hash.Hasher;
import org.gradle.internal.hash.Hashing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;

public class CachingDexClassLoader extends ClassLoader implements ImplementationHashAware {

    private final Hasher hasher = Hashing.newHasher();

    private final FileHasher fileHasher = new DefaultFileHasher(new DefaultStreamHasher());

    private DexClassLoader dexClassLoader;


    @Override
    public HashCode getImplementationHash() {
        return hasher.hash();
    }


    private void tryLoadDexFile(File url, ClassLoader parent) {

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

    private File compileAndLoadDex(byte[] bytes, ClassLoader parent) {
        File compileDir = new File(new AppDataDirGuesser().guess(), "dexCache");
        if (!compileDir.exists()) {
            compileDir.mkdirs();
        }
        HashCode hashCode = Hashing.hashBytes(bytes);
        File dexFile = new File(compileDir, "Generated_" + hashCode + ".jar");
        if (dexFile.exists()) {
            tryLoadDexFile(dexFile, parent);
            return dexFile;
        }
        DexCompiler.INSTANCE.compileClassByteCode(Collections.singletonList(bytes), dexFile);
        tryLoadDexFile(dexFile, parent);
        return dexFile;
    }


    public Class<?> defineClass(String name, byte[] bytes, ClassLoader parent) throws
            ClassNotFoundException {
        File dexFile = compileAndLoadDex(bytes, parent);
        hasher.putHash(fileHasher.hash(dexFile));

        return findClass(name);

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
    protected URL findResource(String name) {
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
    protected Enumeration<URL> findResources(String name) throws IOException {
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
}
