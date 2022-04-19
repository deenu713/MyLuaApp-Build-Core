/*
 * Copyright 2018 the original author or authors.
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
package org.gradle.internal.metaobject;


import org.gradle.api.internal.DynamicObjectAware;
import org.gradle.internal.Cast;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.reflect.JavaPropertyReflectionUtil;
import org.gradle.internal.state.ModelObject;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link DynamicObject} which uses groovy reflection to provide access to the properties and methods of a bean.
 *
 * <p>Uses some deep hacks to avoid some expensive reflections and the use of exceptions when a particular property or method cannot be found,
 * for example, when a decorated object is used as the delegate of a configuration closure. Also uses some hacks to insert some customised type
 * coercion and error reporting. Enjoy.
 */
public class BeanDynamicObject extends AbstractDynamicObject {


    private final Object bean;
    private final boolean includeProperties;
    private final MetaClassAdapter delegate;
    private final boolean implementsMissing;
    @Nullable
    private final Class<?> publicType;

    private BeanDynamicObject withNoProperties;
    private BeanDynamicObject withNoImplementsMissing;



    public BeanDynamicObject(Object bean) {
        this(bean, null, true, true);
    }

    public BeanDynamicObject(Object bean, @Nullable Class<?> publicType) {
        this(bean, publicType, true,true);
    }

    BeanDynamicObject(Object bean, @Nullable Class<?> publicType, boolean includeProperties, boolean implementsMissing) {
        if (bean == null) {
            throw new IllegalArgumentException("Value is null");
        }
        this.bean = bean;
        this.publicType = publicType;
        this.includeProperties = includeProperties;
        this.implementsMissing = implementsMissing;

        this.delegate = determineDelegate(bean);
    }

    public MetaClassAdapter determineDelegate(Object bean) {
        return new MetaClassAdapter();
    }

    public BeanDynamicObject withNoProperties() {
        if (!includeProperties) {
            return this;
        }
        if (withNoProperties == null) {
            withNoProperties = new BeanDynamicObject(bean, publicType, false, implementsMissing);
        }
        return withNoProperties;
    }

    public BeanDynamicObject withNotImplementsMissing() {
        if (!implementsMissing) {
            return this;
        }
        if (withNoImplementsMissing == null) {
            withNoImplementsMissing = new BeanDynamicObject(bean, publicType, includeProperties, false);
        }
        return withNoImplementsMissing;
    }

    @Override
    public String getDisplayName() {
        return bean.toString();
    }

    @Nullable
    @Override
    public Class<?> getPublicType() {
        return publicType != null ? publicType : bean.getClass();
    }

    @Override
    public boolean hasUsefulDisplayName() {
        if (bean instanceof ModelObject) {
            return ((ModelObject) bean).hasUsefulDisplayName();
        }
        return !JavaPropertyReflectionUtil.hasDefaultToString(bean);
    }


    @Override
    public boolean hasProperty(String name) {
        return delegate.hasProperty(name);
    }

    @Override
    public DynamicInvokeResult tryGetProperty(String name) {
        return delegate.getProperty(name);
    }

    @Override
    public DynamicInvokeResult trySetProperty(String name, Object value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public Map<String, ?> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public boolean hasMethod(String name, Object... arguments) {
        return delegate.hasMethod(name, arguments);
    }

    @Override
    public DynamicInvokeResult tryInvokeMethod(String name, Object... arguments) {
        return delegate.invokeMethod(name, arguments);
    }

    private class MetaClassAdapter {
        protected String getDisplayName() {
            return BeanDynamicObject.this.getDisplayName();
        }

        public boolean hasProperty(String name) {
            if (!includeProperties) {
                return false;
            }

            if (bean instanceof PropertyMixIn) {
                PropertyMixIn propertyMixIn = (PropertyMixIn) bean;
                return propertyMixIn.getAdditionalProperties().hasProperty(name);
            }
            return false;
        }

        public DynamicInvokeResult getProperty(String name) {
            if (!includeProperties) {
                return DynamicInvokeResult.notFound();
            }


            if (bean instanceof PropertyMixIn) {
                PropertyMixIn propertyMixIn = (PropertyMixIn) bean;
                return propertyMixIn.getAdditionalProperties().tryGetProperty(name);
                // Do not check for opaque properties when implementing PropertyMixIn, as this is expensive
            }

            if (!implementsMissing) {
                return DynamicInvokeResult.notFound();
            }


            return getOpaqueProperty(name);
        }

        protected DynamicInvokeResult getOpaqueProperty(String name) {
            return DynamicInvokeResult.notFound();
        }


        public DynamicInvokeResult setProperty(final String name, Object value) {
            if (!includeProperties) {
                return DynamicInvokeResult.notFound();
            }

            if (bean instanceof PropertyMixIn) {
                PropertyMixIn propertyMixIn = (PropertyMixIn) bean;
                return propertyMixIn.getAdditionalProperties().trySetProperty(name, value);
                // When implementing PropertyMixIn, do not check for opaque properties, as this can be expensive
            }


            return DynamicInvokeResult.notFound();


        }


        public Map<String, ?> getProperties() {
            if (!includeProperties) {
                return Collections.emptyMap();
            }

            Map<String, Object> properties = new HashMap<String, Object>();

            if (bean instanceof PropertyMixIn) {
                PropertyMixIn propertyMixIn = (PropertyMixIn) bean;
                properties.putAll(propertyMixIn.getAdditionalProperties().getProperties());
            }
            getOpaqueProperties(properties);
            return properties;
        }

        protected void getOpaqueProperties(Map<String, Object> properties) {
        }

        public boolean hasMethod(final String name, final Object... arguments) {

            if (bean instanceof MethodMixIn) {
                MethodMixIn methodMixIn = (MethodMixIn) bean;
                return methodMixIn.getAdditionalMethods().hasMethod(name, arguments);
            }
            return false;
        }

        private Class[] inferTypes(Object[] arguments) {

            Class[] classes = new Class[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                Object argType = arguments[i];
                if (argType == null) {
                    classes[i] = null;
                } else {
                    classes[i] = argType.getClass();
                }
            }
            return classes;
        }

        public DynamicInvokeResult invokeMethod(String name, Object... arguments) {


            if (bean instanceof MethodMixIn) {
                // If implements MethodMixIn, do not attempt to locate opaque method, as this is expensive
                MethodMixIn methodMixIn = (MethodMixIn) bean;
                return methodMixIn.getAdditionalMethods().tryInvokeMethod(name, arguments);
            }


            return DynamicInvokeResult.notFound();


        }



    /*
       The GroovyObject interface defines dynamic property and dynamic method methods. Implementers
       are free to implement their own logic in these methods which makes it invisible to the metaclass.

       The most notable case of this is Closure.

       So in this case we use these methods directly on the GroovyObject in case it does implement logic at this level.
     */


        private class MapAdapter extends MetaClassAdapter {
            Map<String, Object> map = Cast.uncheckedNonnullCast(bean);

            @Override
            public boolean hasProperty(String name) {
                return map.containsKey(name) || super.hasProperty(name);
            }

            @Override
            protected DynamicInvokeResult getOpaqueProperty(String name) {
                return DynamicInvokeResult.found(map.get(name));
            }

            @Override
            protected void getOpaqueProperties(Map<String, Object> properties) {
                properties.putAll(map);
            }


        }
    }
}
