/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.plugins;

import com.google.common.base.Throwables;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public abstract class AbstractBeanLocatorCallable<V> implements Callable<V> {
  private Map<BlockingQueue<Object>, BlockingQueue<Object>> waitingList =
      new IdentityHashMap<BlockingQueue<Object>, BlockingQueue<Object>>();
  protected boolean submitted;

  protected final PrivatePluginBeanLocator locator;
  private boolean finished;

  public AbstractBeanLocatorCallable(PrivatePluginBeanLocator locator) {
    this.locator = locator;
  }

  public synchronized void submit(ExecutorService service) {
    if (!submitted) {
      submitted = true;
      service.submit(this);
    }
  }

  public synchronized void addWaiter(BlockingQueue<Object> queue) {
    if (finished) {
      queue.add(this);
    } else {
      waitingList.put(queue, queue);
    }
  }

  public synchronized void finished() {
    finished = true;
    locator.clearCallable();
    for (BlockingQueue<Object> waiter : waitingList.keySet()) {
      waiter.add(this);
    }
  }

  @Override
  public V call() throws Exception {
    try {
      return doWork();
    } catch (Throwable t) {
      locator.setThrowable(t);
      throw Throwables.propagate(t);
    } finally {
      finished();
    }
  }

  protected abstract V doWork() throws Exception;
}
