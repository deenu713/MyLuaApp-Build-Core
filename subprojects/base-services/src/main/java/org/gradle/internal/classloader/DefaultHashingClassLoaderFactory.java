/*
 * Copyright 2016 the original author or authors.
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

import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.classpath.DefaultClassPath;
import org.gradle.internal.hash.HashCode;
import org.gradle.internal.hash.Hasher;
import org.gradle.internal.hash.Hashing;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;

public class DefaultHashingClassLoaderFactory extends DefaultClassLoaderFactory implements HashingClassLoaderFactory {
    private final ClasspathHasher classpathHasher;
    private final Map<ClassLoader, HashCode> hashCodes = Collections.synchronizedMap(new WeakHashMap<ClassLoader, HashCode>());

    public DefaultHashingClassLoaderFactory(ClasspathHasher classpathHasher) {
        this.classpathHasher = classpathHasher;
    }

    @Override
    protected ClassLoader doCreateClassLoader(String name, ClassLoader parent, ClassPath classPath) {
        ClassLoader classLoader = super.doCreateClassLoader(name, parent, classPath);
        hashCodes.put(classLoader, calculateClassLoaderHash(classPath));
        return classLoader;
    }

    @Override
    protected ClassLoader doCreateFilteringClassLoader(ClassLoader parent, FilteringClassLoader.Spec spec) {
        ClassLoader classLoader = super.doCreateFilteringClassLoader(parent, spec);
        hashCodes.put(classLoader, calculateFilterSpecHash(spec));
        return classLoader;
    }

    @Override
    public ClassLoader createChildClassLoader(String name, ClassLoader parent, ClassPath classPath, HashCode implementationHash) {
        HashCode hashCode = implementationHash != null
            ? implementationHash
            : calculateClassLoaderHash(classPath);
        ClassLoader classLoader = super.doCreateClassLoader(name, parent, classPath);
        hashCodes.put(classLoader, hashCode);
        return classLoader;
    }

    @Override
    public HashCode getClassLoaderClasspathHash(ClassLoader classLoader) {
        if (classLoader instanceof ImplementationHashAware) {
            ImplementationHashAware loader = (ImplementationHashAware) classLoader;
            return loader.getImplementationHash();
        }
        if (classLoader instanceof DexClassLoader) {
            try {
                Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
                pathListField.setAccessible(true);

                Object pathList = pathListField.get(classLoader);

                Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
                dexElementsField.setAccessible(true);

                Object[] dexElements = (Object[]) dexElementsField.get(pathList);

                String[] dexPaths = new String[dexElements.length];

                for (int i = 0; i < dexElements.length; i++) {

                    Field dexFilesFiled = dexElements[i].getClass().getDeclaredField("dexFile");
                    dexFilesFiled.setAccessible(true);

                    Object dexFile = dexFilesFiled.get(dexElements[i]);

                    Field dexPathField = dexFile.getClass().getDeclaredField("mFileName");
                    dexPathField.setAccessible(true);

                    dexPaths[i] = (String) dexPathField.get(dexFile);
                }

                return classpathHasher.hash(
                        DefaultClassPath.of(
                                Arrays.stream(dexPaths)
                                        .map(File::new)
                                        .toArray(File[]::new)
                        )
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return hashCodes.get(classLoader);
    }

    private HashCode calculateClassLoaderHash(ClassPath classPath) {
        return classpathHasher.hash(classPath);
    }

    private static HashCode calculateFilterSpecHash(FilteringClassLoader.Spec spec) {
        Hasher hasher = Hashing.newHasher();
        addToHash(hasher, spec.getClassNames());
        addToHash(hasher, spec.getPackageNames());
        addToHash(hasher, spec.getPackagePrefixes());
        addToHash(hasher, spec.getResourcePrefixes());
        addToHash(hasher, spec.getResourceNames());
        addToHash(hasher, spec.getDisallowedClassNames());
        addToHash(hasher, spec.getDisallowedPackagePrefixes());
        return hasher.hash();
    }

    private static void addToHash(Hasher hasher, Set<String> items) {
        int count = items.size();
        hasher.putInt(count);
        if (count == 0) {
            return;
        }
        String[] sortedItems = items.toArray(new String[count]);
        Arrays.sort(sortedItems);
        for (String item : sortedItems) {
            hasher.putString(item);
        }
    }
}
