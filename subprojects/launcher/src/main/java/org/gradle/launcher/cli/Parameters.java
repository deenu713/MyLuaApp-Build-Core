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
import org.gradle.launcher.configuration.BuildLayoutResult;

public class Parameters {
    private final BuildLayoutResult layout;
    private final StartParameterInternal startParameter;
    private final Object daemonParameters;

    public Parameters(BuildLayoutResult layout, StartParameterInternal startParameter, Object daemonParameters) {
        this.layout = layout;
        this.startParameter = startParameter;
        this.daemonParameters = daemonParameters;
    }

    public Object getDaemonParameters() {
        return daemonParameters;
    }

    public StartParameterInternal getStartParameter() {
        return startParameter;
    }

    public BuildLayoutResult getLayout() {
        return layout;
    }
}
