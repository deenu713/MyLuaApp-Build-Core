package org.gradle.api;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileTree;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.WorkResult;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;

public interface Project {

    /**
     * The default project build file name.
     */
    String DEFAULT_BUILD_FILE = "build.gradle";

    /**
     * The hierarchy separator for project and task path names.
     */
    String PATH_SEPARATOR = ":";

    /**
     * The default build directory name.
     */
    String DEFAULT_BUILD_DIR_NAME = "build";

    String GRADLE_PROPERTIES = "gradle.properties";

    String SYSTEM_PROP_PREFIX = "systemProp";

    String DEFAULT_VERSION = "unspecified";

    String DEFAULT_STATUS = "release";

    /**
     * <p>Returns the name of this project. The project's name is not necessarily unique within a project hierarchy. You
     * should use the {@link #getPath()} method for a unique identifier for the project.</p>
     *
     * @return The name of this project. Never return null.
     */
    String getName();


    /**
     * <p>Returns the path of this project.  The path is the fully qualified name of the project.</p>
     *
     * @return The path. Never returns null.
     */
    String getPath();


    /**
     * <p>Returns the nesting level of a project in a multi-project hierarchy. For single project builds this is always
     * 0. In a multi-project hierarchy 0 is returned for the root project.</p>
     */
    int getDepth();

    /**
     * <p>Returns the tasks of this project.</p>
     *
     * @return the tasks of this project.
     */
    TaskContainer getTasks();

    /**
     * <p>Resolves a file path relative to the project directory of this project. This method converts the supplied path
     * based on its type:</p>
     *
     * <ul>
     *
     * <li>A {@link CharSequence}, including {@link String}. Interpreted relative to the project directory. A string that starts with {@code file:} is treated as a file URL.</li>
     *
     * <li>A {@link File}. If the file is an absolute file, it is returned as is. Otherwise, the file's path is
     * interpreted relative to the project directory.</li>
     *
     * <li>A {@link java.nio.file.Path}. The path must be associated with the default provider and is treated the
     * same way as an instance of {@code File}.</li>
     *
     * <li>A {@link java.net.URI} or {@link java.net.URL}. The URL's path is interpreted as the file path. Only {@code file:} URLs are supported.</li>
     *
     * <li>A {@link org.gradle.api.file.Directory} or {@link org.gradle.api.file.RegularFile}.</li>
     *
     * <li>A {@link Provider} of any supported type. The provider's value is resolved recursively.</li>
     *
     * <li>A {@link org.gradle.api.resources.TextResource}.</li>
     *
     * <li>A {@link java.util.concurrent.Callable} that returns any supported type. The callable's return value is resolved recursively.</li>
     *
     * </ul>
     *
     * @param path The object to resolve as a File.
     * @return The resolved file. Never returns null.
     */
    File file(Object path);

    /**
     * <p>Resolves a file path to a URI, relative to the project directory of this project. Evaluates the provided path
     * object as described for {@link #file(Object)}, with the exception that any URI scheme is supported, not just
     * 'file:' URIs.</p>
     *
     * @param path The object to resolve as a URI.
     * @return The resolved URI. Never returns null.
     */
    URI uri(Object path);

    /**
     * <p>Returns the relative path from the project directory to the given path. The given path object is (logically)
     * resolved as described for {@link #file(Object)}, from which a relative path is calculated.</p>
     *
     * @param path The path to convert to a relative path.
     * @return The relative path. Never returns null.
     * @throws IllegalArgumentException If the given path cannot be relativized against the project directory.
     */
    String relativePath(Object path);

    /**
     * <p>Returns a {@link ConfigurableFileCollection} containing the given files. You can pass any of the following
     * types to this method:</p>
     *
     * <ul> <li>A {@link CharSequence}, including {@link String}. Interpreted relative to the project directory, as per {@link #file(Object)}. A string that starts with {@code file:} is treated as a file URL.</li>
     *
     * <li>A {@link File}. Interpreted relative to the project directory, as per {@link #file(Object)}.</li>
     *
     * <li>A {@link java.nio.file.Path}, as per {@link #file(Object)}.</li>
     *
     * <li>A {@link java.net.URI} or {@link java.net.URL}. The URL's path is interpreted as a file path. Only {@code file:} URLs are supported.</li>
     *
     * <li>A {@link org.gradle.api.file.Directory} or {@link org.gradle.api.file.RegularFile}.</li>
     *
     * <li>A {@link java.util.Collection}, {@link Iterable}, or an array that contains objects of any supported type. The elements of the collection are recursively converted to files.</li>
     *
     * <li>A {@link org.gradle.api.file.FileCollection}. The contents of the collection are included in the returned collection.</li>
     *
     * <li>A {@link org.gradle.api.file.FileTree} or {@link org.gradle.api.file.DirectoryTree}. The contents of the tree are included in the returned collection.</li>
     *
     * <li>A {@link Provider} of any supported type. The provider's value is recursively converted to files. If the provider represents an output of a task, that task is executed if the file collection is used as an input to another task.
     *
     * <li>A {@link java.util.concurrent.Callable} that returns any supported type. The return value of the {@code call()} method is recursively converted to files. A {@code null} return value is treated as an empty collection.</li>
     **
     * <li>A {@link Task}. Converted to the task's output files. The task is executed if the file collection is used as an input to another task.</li>
     *
     * <li>A {@link TaskOutputs}. Converted to the output files the related task. The task is executed if the file collection is used as an input to another task.</li>
     *
     * <li>Anything else is treated as an error.</li>
     *
     * </ul>
     *
     * <p>The returned file collection is lazy, so that the paths are evaluated only when the contents of the file
     * collection are queried. The file collection is also live, so that it evaluates the above each time the contents
     * of the collection is queried.</p>
     *
     * <p>The returned file collection maintains the iteration order of the supplied paths.</p>
     *
     * <p>The returned file collection maintains the details of the tasks that produce the files, so that these tasks are executed if this file collection is used as an input to some task.</p>
     *
     * <p>This method can also be used to create an empty collection, which can later be mutated to add elements.</p>
     *
     * @param paths The paths to the files. May be empty.
     * @return The file collection. Never returns null.
     */
    ConfigurableFileCollection files(Object... paths);


    /**
     * <p>Creates a new {@code ConfigurableFileCollection} using the given paths. The paths are evaluated as per {@link
     * #files(Object...)}. The file collection is configured using the given action. Example:</p>
     * <pre>
     * files "$buildDir/classes" {
     *     builtBy 'compile'
     * }
     * </pre>
     * <p>The returned file collection is lazy, so that the paths are evaluated only when the contents of the file
     * collection are queried. The file collection is also live, so that it evaluates the above each time the contents
     * of the collection is queried.</p>
     *
     * @param paths The contents of the file collection. Evaluated as per {@link #files(Object...)}.
     * @param configureAction The action to use to configure the file collection.
     * @return the configured file tree. Never returns null.
     * @since 3.5
     */
    ConfigurableFileCollection files(Object paths, Action<? super ConfigurableFileCollection> configureAction);

    /**
     * <p>Creates a new {@code ConfigurableFileTree} using the given base directory. The given baseDir path is evaluated
     * as per {@link #file(Object)}.</p>
     *
     * <p>The returned file tree is lazy, so that it scans for files only when the contents of the file tree are
     * queried. The file tree is also live, so that it scans for files each time the contents of the file tree are
     * queried.</p>
     *
     * <pre class='autoTested'>
     * def myTree = fileTree("src")
     * myTree.include "**&#47;*.java"
     * myTree.builtBy "someTask"
     *
     * task copy(type: Copy) {
     *    from myTree
     * }
     * </pre>
     *
     * <p>The order of the files in a {@code FileTree} is not stable, even on a single computer.
     *
     * @param baseDir The base directory of the file tree. Evaluated as per {@link #file(Object)}.
     * @return the file tree. Never returns null.
     */
    ConfigurableFileTree fileTree(Object baseDir);



    /**
     * <p>Creates a new {@code ConfigurableFileTree} using the given base directory. The given baseDir path is evaluated
     * as per {@link #file(Object)}. The action will be used to configure the new file tree. Example:</p>
     *
     * <pre class='autoTested'>
     * def myTree = fileTree('src') {
     *    exclude '**&#47;.data/**'
     *    builtBy 'someTask'
     * }
     *
     * task copy(type: Copy) {
     *    from myTree
     * }
     * </pre>
     *
     * <p>The returned file tree is lazy, so that it scans for files only when the contents of the file tree are
     * queried. The file tree is also live, so that it scans for files each time the contents of the file tree are
     * queried.</p>
     *
     * <p>The order of the files in a {@code FileTree} is not stable, even on a single computer.
     *
     * @param baseDir The base directory of the file tree. Evaluated as per {@link #file(Object)}.
     * @param configureAction Action to configure the {@code ConfigurableFileTree} object.
     * @return the configured file tree. Never returns null.
     * @since 3.5
     */
    ConfigurableFileTree fileTree(Object baseDir, Action<? super ConfigurableFileTree> configureAction);

    /**
     * <p>Creates a new {@code ConfigurableFileTree} using the provided map of arguments.  The map will be applied as
     * properties on the new file tree.  Example:</p>
     *
     * <pre class='autoTested'>
     * def myTree = fileTree(dir:'src', excludes:['**&#47;ignore/**', '**&#47;.data/**'])
     *
     * task copy(type: Copy) {
     *     from myTree
     * }
     * </pre>
     *
     * <p>The returned file tree is lazy, so that it scans for files only when the contents of the file tree are
     * queried. The file tree is also live, so that it scans for files each time the contents of the file tree are
     * queried.</p>
     *
     * <p>The order of the files in a {@code FileTree} is not stable, even on a single computer.
     *
     * @param args map of property assignments to {@code ConfigurableFileTree} object
     * @return the configured file tree. Never returns null.
     */
    ConfigurableFileTree fileTree(Map<String, ?> args);

    /**
     * <p>Creates a new {@code FileTree} which contains the contents of the given ZIP file. The given zipPath path is
     * evaluated as per {@link #file(Object)}. You can combine this method with the {@link #copy(Action)}
     * method to unzip a ZIP file.</p>
     *
     * <p>The returned file tree is lazy, so that it scans for files only when the contents of the file tree are
     * queried. The file tree is also live, so that it scans for files each time the contents of the file tree are
     * queried.</p>
     *
     * @param zipPath The ZIP file. Evaluated as per {@link #file(Object)}.
     * @return the file tree. Never returns null.
     */
    FileTree zipTree(Object zipPath);

    /**
     * Creates a new {@code FileTree} which contains the contents of the given TAR file. The given tarPath path can be:
     * <ul>
     *   <li>an instance of {@link org.gradle.api.resources.Resource}</li>
     *   <li>any other object is evaluated as per {@link #file(Object)}</li>
     * </ul>
     *
     * The returned file tree is lazy, so that it scans for files only when the contents of the file tree are
     * queried. The file tree is also live, so that it scans for files each time the contents of the file tree are
     * queried.
     * <p>
     * Unless custom implementation of resources is passed, the tar tree attempts to guess the compression based on the file extension.
     * <p>
     * You can combine this method with the {@link #copy(groovy.lang.Closure)}
     * method to untar a TAR file:
     *
     * <pre class='autoTested'>
     * task untar(type: Copy) {
     *   from tarTree('someCompressedTar.gzip')
     *
     *   //tar tree attempts to guess the compression based on the file extension
     *   //however if you must specify the compression explicitly you can:
     *   from tarTree(resources.gzip('someTar.ext'))
     *
     *   //in case you work with unconventionally compressed tars
     *   //you can provide your own implementation of a ReadableResource:
     *   //from tarTree(yourOwnResource as ReadableResource)
     *
     *   into 'dest'
     * }
     * </pre>
     *
     * @param tarPath The TAR file or an instance of {@link org.gradle.api.resources.Resource}.
     * @return the file tree. Never returns null.
     */
    FileTree tarTree(Object tarPath);

    /**
     * Creates a {@code Provider} implementation based on the provided value.
     *
     * @param value The {@code java.util.concurrent.Callable} use to calculate the value.
     * @return The provider. Never returns null.
     * @throws org.gradle.api.InvalidUserDataException If the provided value is null.
     * @see org.gradle.api.provider.ProviderFactory#provider(Callable)
     * @since 4.0
     */
    <T> Provider<T> provider(Callable<T> value);

    /**
     * Copies the specified files.  The given action is used to configure a {@link CopySpec}, which is then used to
     * copy the files.
     * @see #copy(Action)
     * @param action Action to configure the CopySpec
     * @return {@link WorkResult} that can be used to check if the copy did any work.
     */
    WorkResult copy(Action<? super CopySpec> action);


    /**
     * Provides access to methods to create various kinds of model objects.
     *
     * @since 4.0
     */
    ObjectFactory getObjects();


    /**
     * <p>Returns the {@link org.gradle.api.invocation.Gradle} invocation which this project belongs to.</p>
     *
     * @return The Gradle object. Never returns null.
     */
    Gradle getGradle();
}
