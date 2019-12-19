/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.logstreams.util;

import io.zeebe.logstreams.log.LogStream;
import io.zeebe.logstreams.log.LogStreamBuilder;
import io.zeebe.logstreams.spi.LogStorage;
import io.zeebe.util.sched.Actor;
import io.zeebe.util.sched.ActorScheduler;
import io.zeebe.util.sched.future.ActorFuture;
import io.zeebe.util.sched.future.CompletableActorFuture;

public final class SyncLogStreamBuilder implements LogStreamBuilder {
  private final LogStreamBuilder delegate;
  private ActorScheduler actorScheduler;

  SyncLogStreamBuilder() {
    this(LogStream.builder());
  }

  SyncLogStreamBuilder(final LogStreamBuilder delegate) {
    this.delegate = delegate;
  }

  @Override
  public SyncLogStreamBuilder withActorScheduler(final ActorScheduler actorScheduler) {
    delegate.withActorScheduler(actorScheduler);
    return this;
  }

  @Override
  public SyncLogStreamBuilder withMaxFragmentSize(final int maxFragmentSize) {
    delegate.withMaxFragmentSize(maxFragmentSize);
    return this;
  }

  @Override
  public SyncLogStreamBuilder withLogStorage(final LogStorage logStorage) {
    delegate.withLogStorage(logStorage);
    return this;
  }

  @Override
  public SyncLogStreamBuilder withPartitionId(final int partitionId) {
    delegate.withPartitionId(partitionId);
    return this;
  }

  @Override
  public SyncLogStreamBuilder withLogName(final String logName) {
    delegate.withLogName(logName);
    return this;
  }

  @Override
  public ActorFuture<LogStream> buildAsync() {
    return delegate.buildAsync();
  }

  public SyncLogStream build(final ActorScheduler scheduler) {
    final var buildFuture = new CompletableActorFuture<SyncLogStream>();
    scheduler.submitActor(
        new Actor() {
          @Override
          protected void onActorStarting() {
            actor.runOnCompletionBlockingCurrentPhase(
                buildAsync(),
                (logStream, t) -> {
                  if (t == null) {
                    buildFuture.complete(new SyncLogStream(logStream));
                  } else {
                    buildFuture.completeExceptionally(t);
                  }
                });
          }
        });
    return buildFuture.join();
  }
}
