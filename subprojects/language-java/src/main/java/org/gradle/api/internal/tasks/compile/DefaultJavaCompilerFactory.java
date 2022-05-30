/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.tasks.compile;

import org.gradle.api.internal.ClassPathRegistry;
import org.gradle.api.internal.tasks.compile.processing.AnnotationProcessorDetector;
import org.gradle.internal.Factory;
import org.gradle.jvm.toolchain.internal.JavaCompilerFactory;
import org.gradle.language.base.internal.compile.CompileSpec;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.process.internal.ExecHandleFactory;
import org.gradle.process.internal.JavaForkOptionsFactory;

import javax.tools.JavaCompiler;

public class DefaultJavaCompilerFactory implements JavaCompilerFactory {

    private final JavaForkOptionsFactory forkOptionsFactory;
    private final ExecHandleFactory execHandleFactory;
    private final AnnotationProcessorDetector processorDetector;
    private final ClassPathRegistry classPathRegistry;

    private Factory<JavaCompiler> javaHomeBasedJavaCompilerFactory;

    public DefaultJavaCompilerFactory(JavaForkOptionsFactory forkOptionsFactory, ExecHandleFactory execHandleFactory, AnnotationProcessorDetector processorDetector, ClassPathRegistry classPathRegistry) {

        this.forkOptionsFactory = forkOptionsFactory;
        this.execHandleFactory = execHandleFactory;
        this.processorDetector = processorDetector;
        this.classPathRegistry = classPathRegistry;

    }

    private Factory<JavaCompiler> getJavaHomeBasedJavaCompilerFactory() {
        if (javaHomeBasedJavaCompilerFactory == null) {
            javaHomeBasedJavaCompilerFactory = new JavaHomeBasedJavaCompilerFactory(classPathRegistry.getClassPath("JAVA-COMPILER-PLUGIN").getAsFiles());
        }
        return javaHomeBasedJavaCompilerFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CompileSpec> Compiler<T> create(Class<T> type) {
        Compiler<T> result = createTargetCompiler(type);
        return (Compiler<T>) new ModuleApplicationNameWritingCompiler<>(new AnnotationProcessorDiscoveringCompiler<>(new NormalizingJavaCompiler((Compiler<JavaCompileSpec>) result), processorDetector));
    }

    @SuppressWarnings("unchecked")
    private <T extends CompileSpec> Compiler<T> createTargetCompiler(Class<T> type) {
        if (!JavaCompileSpec.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(String.format("Cannot create a compiler for a spec with type %s", type.getSimpleName()));
        }

        if (CommandLineJavaCompileSpec.class.isAssignableFrom(type)) {
            return (Compiler<T>) new CommandLineJavaCompiler(execHandleFactory);
        }

        return (Compiler<T>) new JdkJavaCompiler(getJavaHomeBasedJavaCompilerFactory());

    }
}
