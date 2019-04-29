/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Stolen from
 * https://today.java.net/pub/a/today/2008/10/23/creating-a-notifying-blocking-thread-pool-executor.html
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {
  public BlockingThreadPoolExecutor(
      int poolSize,
      int queueSize,
      long keepAliveTime,
      TimeUnit keepAliveTimeUnit,
      long maxBlockingTime,
      TimeUnit maxBlockingTimeUnit,
      ThreadFactory threadFactory,
      Callable<Boolean> blockingTimeCallback) {
    super(
        poolSize,
        poolSize,
        keepAliveTime,
        keepAliveTimeUnit,
        new ArrayBlockingQueue<Runnable>(Math.max(poolSize, queueSize)),
        threadFactory,
        new BlockThenRunPolicy(maxBlockingTime, maxBlockingTimeUnit, blockingTimeCallback));
    super.allowCoreThreadTimeOut(true);
  }

  @Override
  public void setRejectedExecutionHandler(RejectedExecutionHandler h) {
    throw new UnsupportedOperationException(
        "setRejectedExecutionHandler is not allowed on this class.");
  }

  private static class BlockThenRunPolicy implements RejectedExecutionHandler {
    private long blockTimeout;
    private TimeUnit blocTimeoutUnit;
    private Callable<Boolean> blockTimeoutCallback;

    public BlockThenRunPolicy(
        long blockTimeout, TimeUnit blocTimeoutUnit, Callable<Boolean> blockTimeoutCallback) {
      this.blockTimeout = blockTimeout;
      this.blocTimeoutUnit = blocTimeoutUnit;
      this.blockTimeoutCallback = blockTimeoutCallback;
    }

    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
      BlockingQueue<Runnable> queue = executor.getQueue();
      boolean taskSent = false;

      while (!taskSent) {
        if (executor.isShutdown()) {
          throw new RejectedExecutionException(
              "ThreadPoolExecutor has shutdown while attempting to offer a new task.");
        }

        try {
          if (queue.offer(task, blockTimeout, blocTimeoutUnit)) {
            taskSent = true;
          } else {
            Boolean result = null;
            try {
              result = blockTimeoutCallback.call();
            } catch (Exception e) {
              throw new RejectedExecutionException(e);
            }
            if (result == false) {
              throw new RejectedExecutionException(
                  "User decided to stop waiting for task insertion");
            } else {
              // user decided to keep waiting (may log it)
              continue;
            }
          }
        } catch (InterruptedException e) {
          // we need to go back to the offer call...
        }
      }
    }
  }
}
