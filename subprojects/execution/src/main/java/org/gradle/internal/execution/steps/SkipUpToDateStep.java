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

package org.gradle.internal.execution.steps;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import org.apache.commons.lang.StringUtils;
import org.gradle.caching.internal.origin.OriginMetadata;
import org.gradle.internal.Try;
import org.gradle.internal.execution.ExecutionOutcome;
import org.gradle.internal.execution.ExecutionResult;
import org.gradle.internal.execution.UnitOfWork;
import org.gradle.internal.execution.history.AfterPreviousExecutionState;
import org.gradle.internal.snapshot.FileSystemSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;

public class SkipUpToDateStep<C extends IncrementalChangesContext> implements Step<C, UpToDateResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkipUpToDateStep.class);

    private static final ImmutableList<String> CHANGE_TRACKING_DISABLED = ImmutableList.of("Change tracking is disabled.");

    private final Step<? super C, ? extends CurrentSnapshotResult> delegate;

    public SkipUpToDateStep(Step<? super C, ? extends CurrentSnapshotResult> delegate) {
        this.delegate = delegate;
    }

    @Override
    public UpToDateResult execute(UnitOfWork work, C context) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Determining if {} is up-to-date", work.getDisplayName());
        }
        return context.getChanges().map(changes -> {
            ImmutableList<String> reasons = changes.getAllChangeMessages();
            if (reasons.isEmpty()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Skipping {} as it is up-to-date.", work.getDisplayName());
                }
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                AfterPreviousExecutionState afterPreviousExecutionState = context.getAfterPreviousExecutionState().get();
                return new UpToDateResult() {
                    @Override
                    public ImmutableList<String> getExecutionReasons() {
                        return ImmutableList.of();
                    }

                    @Override
                    public ImmutableSortedMap<String, FileSystemSnapshot> getOutputFilesProduceByWork() {
                        return afterPreviousExecutionState.getOutputFilesProducedByWork();
                    }

                    @Override
                    public Optional<OriginMetadata> getReusedOutputOriginMetadata() {
                        return Optional.of(afterPreviousExecutionState.getOriginMetadata());
                    }

                    @Override
                    public Try<ExecutionResult> getExecutionResult() {
                        return Try.successful(new ExecutionResult() {
                            @Override
                            public ExecutionOutcome getOutcome() {
                                return ExecutionOutcome.UP_TO_DATE;
                            }

                            @Override
                            public Object getOutput() {
                                return work.loadRestoredOutput(context.getWorkspace());
                            }
                        });
                    }

                    @Override
                    public Duration getDuration() {
                        return afterPreviousExecutionState.getOriginMetadata().getExecutionTime();
                    }
                };
            } else {
                return executeBecause(work, reasons, context);
            }
        }).orElseGet(() -> executeBecause(work, CHANGE_TRACKING_DISABLED, context));
    }

    private UpToDateResult executeBecause(UnitOfWork work, ImmutableList<String> reasons, C context) {
        logExecutionReasons(reasons, work);
        CurrentSnapshotResult result = delegate.execute(work, context);
        return new UpToDateResult() {
            @Override
            public ImmutableList<String> getExecutionReasons() {
                return reasons;
            }

            @Override
            public ImmutableSortedMap<String, FileSystemSnapshot> getOutputFilesProduceByWork() {
                return result.getOutputFilesProduceByWork();
            }

            @Override
            public Optional<OriginMetadata> getReusedOutputOriginMetadata() {
                return result.isReused()
                    ? Optional.of(result.getOriginMetadata())
                    : Optional.empty();
            }

            @Override
            public Duration getDuration() {
                return result.getDuration();
            }

            @Override
            public Try<ExecutionResult> getExecutionResult() {
                return result.getExecutionResult();
            }
        };
    }

    private void logExecutionReasons(List<String> reasons, UnitOfWork work) {
        if (LOGGER.isInfoEnabled()) {
            Formatter formatter = new Formatter();
            formatter.format("%s is not up-to-date because:", StringUtils.capitalize(work.getDisplayName()));
            for (String message : reasons) {
                formatter.format("%n  %s", message);
            }
            LOGGER.info(formatter.toString());
        }
    }
}
