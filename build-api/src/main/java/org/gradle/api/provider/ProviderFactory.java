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

package org.gradle.api.provider;

import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.NonExtensible;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;

/**
 * A factory for creating instances of {@link Provider}.
 *
 * <p>
 * An instance of the factory can be injected into a task, plugin or other object by annotating a public constructor or property getter method with {@code javax.inject.Inject}.
 * It is also available via  and {@link Settings#getProviders()}.
 *
 * @since 4.0
 */
@NonExtensible
public interface ProviderFactory {

    /**
     * Creates a {@link Provider} whose value is calculated using the given {@link Callable}.
     *
     * <p>The provider is live and will call the {@link Callable} each time its value is queried. The {@link Callable} may return {@code null}, in which case the provider is considered to have no value.
     *
     * @param value The {@code java.util.concurrent.Callable} use to calculate the value.
     * @return The provider. Never returns null.
     */
    <T> Provider<T> provider(Callable<? extends T> value);

    /**
     * Creates a {@link Provider} whose value is fetched from the environment variable with the given name.
     *
     * The returned provider cannot be queried at configuration time but can produce a configuration time provider
     * via {@link Provider#forUseAtConfigurationTime()}.
     *
     * @param variableName The name of the environment variable.
     * @return The provider. Never returns null.
     * @since 6.1
     */
    Provider<String> environmentVariable(String variableName);

    /**
     * Creates a {@link Provider} whose value is fetched from the environment variable with the given name.
     *
     * The returned provider cannot be queried at configuration time but can produce a configuration time provider
     * via {@link Provider#forUseAtConfigurationTime()}.
     *
     * @param variableName The provider for the name of the environment variable; when the given provider has no value, the returned provider has no value.
     * @return The provider. Never returns null.
     * @since 6.1
     */
    Provider<String> environmentVariable(Provider<String> variableName);

    /**
     * Creates a {@link Provider} whose value is fetched from system properties using the given property name.
     *
     * The returned provider cannot be queried at configuration time but can produce a configuration time provider
     * via {@link Provider#forUseAtConfigurationTime()}.
     *
     * @param propertyName the name of the system property
     * @return the provider for the system property, never returns null
     * @since 6.1
     */
    Provider<String> systemProperty(String propertyName);

    /**
     * Creates a {@link Provider} whose value is fetched from system properties using the given property name.
     *
     * The returned provider cannot be queried at configuration time but can produce a configuration time provider
     * via {@link Provider#forUseAtConfigurationTime()}.
     *
     * @param propertyName the name of the system property
     * @return the provider for the system property, never returns null
     * @since 6.1
     */
    Provider<String> systemProperty(Provider<String> propertyName);

    /**
     * Creates a {@link Provider} whose value is fetched from the Gradle property of the given name.
     *
     * The returned provider cannot be queried at configuration time but can produce a configuration time provider
     * via {@link Provider#forUseAtConfigurationTime()}.
     *
     * @param propertyName the name of the Gradle property
     * @return the provider for the Gradle property, never returns null
     * @since 6.2
     */
    Provider<String> gradleProperty(String propertyName);

    /**
     * Creates a {@link Provider} whose value is fetched from the Gradle property of the given name.
     *
     * The returned provider cannot be queried at configuration time but can produce a configuration time provider
     * via {@link Provider#forUseAtConfigurationTime()}.
     *
     * @param propertyName the name of the Gradle property
     * @return the provider for the Gradle property, never returns null
     * @since 6.2
     */
    Provider<String> gradleProperty(Provider<String> propertyName);


    /**
     * Returns a provider which value will be computed by combining a provider value with another
     * provider value using the supplied combiner function.
     *
     * If the supplied providers represents a task or the output of a task, the resulting provider
     * will carry the dependency information.
     *
     * @param first the first provider to combine with
     * @param second the second provider to combine with
     * @param combiner the combiner of values
     * @param <A> the type of the first provider
     * @param <B> the type of the second provider
     * @param <R> the type of the result of the combiner
     * @return a combined provider
     *
     * @since 6.6
     */
    <A, B, R> Provider<R> zip(Provider<A> first, Provider<B> second, BiFunction<A, B, R> combiner);
}
