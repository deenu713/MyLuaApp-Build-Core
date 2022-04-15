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
package org.gradle.api.internal;


import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Namer;
import org.gradle.internal.Cast;
import org.gradle.internal.Transformers;
import org.gradle.internal.reflect.Instantiator;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class AbstractPolymorphicDomainObjectContainer<T>
        extends AbstractNamedDomainObjectContainer<T> implements PolymorphicDomainObjectContainerInternal<T> {

    protected AbstractPolymorphicDomainObjectContainer(Class<T> type, Instantiator instantiator, Namer<? super T> namer, CollectionCallbackActionDecorator callbackDecorator) {
        super(type, instantiator, namer, callbackDecorator);
    }

    protected abstract <U extends T> U doCreate(String name, Class<U> type);

    @Override
    public <U extends T> U create(String name, Class<U> type) {
        assertMutable("create(String, Class)");
        return create(name, type, null);
    }

    @Override
    public <U extends T> U maybeCreate(String name, Class<U> type) throws InvalidUserDataException {
        T item = findByName(name);
        if (item != null) {
            return Transformers.cast(type).transform(item);
        }
        return create(name, type);
    }

    @Override
    public <U extends T> U create(String name, Class<U> type, Action<? super U> configuration) {
        assertMutable("create(String, Class, Action)");
        assertCanAdd(name);
        U object = doCreate(name, type);
        add(object);
        if (configuration != null) {
            configuration.execute(object);
        }
        return object;
    }

    @Override
    public <U extends T> NamedDomainObjectProvider<U> register(String name, Class<U> type) throws InvalidUserDataException {
        assertMutable("register(String, Class)");
        return createDomainObjectProvider(name, type, null);
    }

    @Override
    public <U extends T> NamedDomainObjectProvider<U> register(String name, Class<U> type, Action<? super U> configurationAction) throws InvalidUserDataException {
        assertMutable("register(String, Class, Action)");
        return createDomainObjectProvider(name, type, configurationAction);
    }

    protected <U extends T> NamedDomainObjectProvider<U> createDomainObjectProvider(String name, Class<U> type, @Nullable Action<? super U> configurationAction) {
        assertCanAdd(name);
        NamedDomainObjectProvider<U> provider = Cast.uncheckedCast(
            getInstantiator().newInstance(NamedDomainObjectCreatingProvider.class, AbstractPolymorphicDomainObjectContainer.this, name, type, configurationAction)
        );
        addLater(provider);
        return provider;
    }

    // Cannot be private due to reflective instantiation
    public class NamedDomainObjectCreatingProvider<I extends T> extends AbstractDomainObjectCreatingProvider<I> {
        public NamedDomainObjectCreatingProvider(String name, Class<I> type, @Nullable Action<? super I> configureAction) {
            super(name, type, configureAction);
        }

        @Override
        protected I createDomainObject() {
            return doCreate(getName(), getType());
        }
    }



    @Override
    public <U extends T> NamedDomainObjectContainer<U> containerWithType(Class<U> type) {
        return Cast.uncheckedNonnullCast(getInstantiator().newInstance(TypedDomainObjectContainerWrapper.class, type, this));
    }

}
