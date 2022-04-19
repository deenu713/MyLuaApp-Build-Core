/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.util.internal;


import org.gradle.api.Action;
import org.gradle.internal.metaobject.DynamicObjectUtil;
import org.gradle.internal.Actions;

import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.DynamicObject;
import org.gradle.util.Configurable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static org.gradle.util.internal.CollectionUtils.toStringList;

public class ConfigureUtil {

    public static <T> T configureByMap(Map<?, ?> properties, T delegate) {
        if (properties.isEmpty()) {
            return delegate;
        }
        DynamicObject dynamicObject = DynamicObjectUtil.asDynamicObject(delegate);

        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            String name = entry.getKey().toString();
            Object value = entry.getValue();

            DynamicInvokeResult result = dynamicObject.trySetProperty(name, value);
            if (result.isFound()) {
                continue;
            }

            result = dynamicObject.tryInvokeMethod(name, value);
            if (!result.isFound()) {
                throw dynamicObject.setMissingProperty(name);
            }
        }

        return delegate;
    }

    public static <T> T configureByMap(Map<?, ?> properties, T delegate, Collection<?> mandatoryKeys) {
        if (!mandatoryKeys.isEmpty()) {
            Collection<String> missingKeys = toStringList(mandatoryKeys);
            missingKeys.removeAll(toStringList(properties.keySet()));
            if (!missingKeys.isEmpty()) {
                throw new IncompleteInputException("Input configuration map does not contain following mandatory keys: " + missingKeys, missingKeys);
            }
        }
        return configureByMap(properties, delegate);
    }

    public static class IncompleteInputException extends RuntimeException {
        private final Collection missingKeys;

        public IncompleteInputException(String message, Collection missingKeys) {
            super(message);
            this.missingKeys = missingKeys;
        }

        public Collection getMissingKeys() {
            return missingKeys;
        }
    }
}

