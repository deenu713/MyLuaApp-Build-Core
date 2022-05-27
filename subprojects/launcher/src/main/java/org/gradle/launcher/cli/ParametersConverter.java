/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.launcher.cli;

import org.gradle.api.internal.StartParameterInternal;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.cli.CommandLineArgumentException;
import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;
import org.gradle.initialization.layout.BuildLayoutFactory;
import org.gradle.launcher.configuration.AllProperties;
import org.gradle.launcher.cli.converter.BuildLayoutConverter;
import org.gradle.launcher.configuration.BuildLayoutResult;
import org.gradle.launcher.cli.converter.BuildOptionBackedConverter;
import org.gradle.launcher.configuration.InitialProperties;
import org.gradle.launcher.cli.converter.InitialPropertiesConverter;
import org.gradle.launcher.cli.converter.LayoutToPropertiesConverter;
import org.gradle.launcher.cli.converter.StartParameterConverter;

import javax.annotation.Nullable;
import java.io.File;

public class ParametersConverter {

    private final InitialPropertiesConverter initialPropertiesConverter;
    private final BuildLayoutConverter buildLayoutConverter;
    private final LayoutToPropertiesConverter layoutToPropertiesConverter;
    private final StartParameterConverter startParameterConverter;

    private final FileCollectionFactory fileCollectionFactory;

    ParametersConverter(InitialPropertiesConverter initialPropertiesConverter,
                        BuildLayoutConverter buildLayoutConverter,
                        LayoutToPropertiesConverter layoutToPropertiesConverter,
                        StartParameterConverter startParameterConverter,
                        FileCollectionFactory fileCollectionFactory) {
        this.initialPropertiesConverter = initialPropertiesConverter;
        this.buildLayoutConverter = buildLayoutConverter;
        this.layoutToPropertiesConverter = layoutToPropertiesConverter;
        this.startParameterConverter = startParameterConverter;
        this.fileCollectionFactory = fileCollectionFactory;
    }

    public ParametersConverter(BuildLayoutFactory buildLayoutFactory, FileCollectionFactory fileCollectionFactory) {
        this(new InitialPropertiesConverter(),
            new BuildLayoutConverter(),
            new LayoutToPropertiesConverter(buildLayoutFactory),
            new StartParameterConverter(),
            fileCollectionFactory);
    }

    public Parameters convert(ParsedCommandLine args, @Nullable File currentDir) throws CommandLineArgumentException {
        InitialProperties initialProperties = initialPropertiesConverter.convert(args);
        BuildLayoutResult buildLayout = buildLayoutConverter.convert(initialProperties, args, currentDir);
        AllProperties properties = layoutToPropertiesConverter.convert(initialProperties, buildLayout, args.getExtraArguments());

        StartParameterInternal startParameter = new StartParameterInternal();
        startParameterConverter.convert(args, buildLayout, properties, startParameter);


        return new Parameters(buildLayout, startParameter, properties);
    }

    public void configure(CommandLineParser parser) {
        initialPropertiesConverter.configure(parser);
        buildLayoutConverter.configure(parser);
        startParameterConverter.configure(parser);

    }
}
