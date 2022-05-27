/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.api.internal.tasks.compile.incremental.recomp;

import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileType;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.internal.FileUtils;
import org.gradle.internal.file.Deleter;
import org.gradle.work.FileChange;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class GroovyRecompilationSpecProvider extends AbstractRecompilationSpecProvider {
    private final boolean incremental;
    private final Iterable<FileChange> sourceChanges;

    public GroovyRecompilationSpecProvider(
        Deleter deleter,
        FileOperations fileOperations,
        FileTree sources,
        boolean incremental,
        Iterable<FileChange> sourceChanges
    ) {
        super(deleter, fileOperations, sources);
        this.incremental = incremental;
        this.sourceChanges = sourceChanges;
    }

    @Override
    protected String getFileExtension() {
        return ".groovy";
    }

    @Override
    public boolean isIncremental() {
        return incremental;
    }

    @Override
    public RecompilationSpec provideRecompilationSpec(CurrentCompilation current, PreviousCompilation previous) {
        RecompilationSpec spec = new RecompilationSpec(previous);
        SourceFileClassNameConverter sourceFileClassNameConverter = getSourceFileClassNameConverter(previous);

        processClasspathChanges(current, previous, spec);
        processOtherChanges(previous, spec, sourceFileClassNameConverter);

        spec.addClassesToProcess(previous.getTypesToReprocess());
        return spec;
    }

    @Override
    public boolean initializeCompilation(JavaCompileSpec spec, RecompilationSpec recompilationSpec) {
        if (!recompilationSpec.isBuildNeeded()) {
            spec.setSourceFiles(Collections.emptySet());
            spec.setClasses(Collections.emptySet());
            return false;
        }
        SourceFileClassNameConverter sourceFileClassNameConverter = getSourceFileClassNameConverter(recompilationSpec.getPreviousCompilation());

        PatternSet classesToDelete = fileOperations.patternSet();
        PatternSet filesToRecompile = fileOperations.patternSet();

        prepareFilePatterns(recompilationSpec.getRelativeSourcePathsToCompile(), classesToDelete, filesToRecompile, sourceFileClassNameConverter);

        spec.setSourceFiles(sourceTree.matching(filesToRecompile));
        includePreviousCompilationOutputOnClasspath(spec);
        addClassesToProcess(spec, recompilationSpec);

        return deleteStaleFilesIn(classesToDelete, spec.getDestinationDir());
    }

    private void prepareFilePatterns(Set<String> relativeSourcePathsToCompile, PatternSet classesToDelete, PatternSet filesToRecompilePatterns, SourceFileClassNameConverter sourceFileClassNameConverter) {
        for (String relativeSourcePath : relativeSourcePathsToCompile) {
            filesToRecompilePatterns.include(relativeSourcePath);

            sourceFileClassNameConverter.getClassNames(relativeSourcePath)
                .stream()
                .map(staleClass -> staleClass.replaceAll("\\.", "/").concat(".class"))
                .forEach(classesToDelete::include);
        }
    }

    private void processOtherChanges(PreviousCompilation previous, RecompilationSpec spec, SourceFileClassNameConverter sourceFileClassNameConverter) {
        if (spec.isFullRebuildNeeded()) {
            return;
        }
        SourceFileChangeProcessor sourceFileChangeProcessor = new SourceFileChangeProcessor(previous);

        for (FileChange fileChange : sourceChanges) {
            if (spec.isFullRebuildNeeded()) {
                return;
            }
            if (fileChange.getFileType() != FileType.FILE) {
                continue;
            }

            File changedFile = fileChange.getFile();
            if (!FileUtils.hasExtension(changedFile, ".groovy")) {
                spec.setFullRebuildCause(rebuildClauseForChangedNonSourceFile("non-Groovy file", fileChange));
                return;
            }

            String relativeFilePath = fileChange.getNormalizedPath();

            Collection<String> changedClasses = sourceFileClassNameConverter.getClassNames(relativeFilePath);
            spec.addRelativeSourcePathToCompile(relativeFilePath);
            sourceFileChangeProcessor.processChange(changedFile, changedClasses, spec);
        }

        for (String className : spec.getClassesToCompile()) {
            if (spec.isFullRebuildNeeded()) {
                return;
            }

            Collection<String> relativeSourceFiles = sourceFileClassNameConverter.getRelativeSourcePaths(className);
            if (relativeSourceFiles.isEmpty()) {
                spec.setFullRebuildCause("unable to find source file of class " + className);
            } else {
                spec.addRelativeSourcePathsToCompile(relativeSourceFiles);
            }
        }
    }
}
