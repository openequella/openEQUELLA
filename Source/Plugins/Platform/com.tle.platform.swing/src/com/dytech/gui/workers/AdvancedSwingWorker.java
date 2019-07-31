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

package com.dytech.gui.workers;

import javax.swing.SwingUtilities;

/**
 * This is a rehash of the well-known SwingWorker, in order to make it easier to use, and allow for
 * better customisation by SwingWorkers extending this. This is fully backwards compatible with the
 * standard SwingWorker, but also allows the user to override <code>exception()</code>. This is
 * called if <code>construct</code> throws an exception. Also, developers wishing to offer
 * customised versions of SwingWorker can override any of the following events:
 *
 * <ul>
 *   <li><code>beforeConstruct()</code>
 *   <li><code>afterConstruct()</code>
 *   <li><code>beforeFinished()</code>
 *   <li><code>afterFinished()</code>
 *   <li><code>beforeException()</code>
 *   <li><code>afterException()</code>
 * </ul>
 *
 * A good example of this is <code>GlassSwingWorker</code>. The original SwingWorker Javadoc is as
 * follows... <hr/> This is the 3rd version of SwingWorker (also known as SwingWorker 3), an
 * abstract class that you subclass to perform GUI-related work in a dedicated thread. For
 * instructions on using this class, see:
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html Note that the API changed
 * slightly in the 3rd version: You must now invoke start() on the SwingWorker after creating it.
 *
 * @author Nicholas Read
 */
public abstract class AdvancedSwingWorker<RESULT> {
  /**
   * Class to maintain reference to current worker thread under separate synchronization control.
   */
  private static class ThreadVar {
    private Thread thread;

    ThreadVar(Thread t) {
      thread = t;
    }

    synchronized void clear() {
      thread = null;
    }

    synchronized Thread get() {
      return thread;
    }
  }

  protected ThreadVar threadVar;
  private Exception exception;
  private RESULT value;

  /** Start a thread that will call the <code>construct</code> method and then exit. */
  public AdvancedSwingWorker() {
    final Runnable doFinished =
        new Runnable() {
          @Override
          public void run() {
            beforeFinished();
            try {
              finished();
            } finally {
              afterFinished();
            }
          }
        };

    final Runnable doException =
        new Runnable() {
          @Override
          public void run() {
            beforeException();
            try {
              exception();
            } finally {
              afterException();
            }
          }
        };

    Runnable doConstruct =
        new Runnable() {
          @Override
          public void run() {
            beforeConstruct();
            try {
              setValue(construct());
            } catch (Exception ex) {
              setActualException(ex);
            } finally {
              afterConstruct();
              threadVar.clear();
            }

            if (getActualException() == null) {
              SwingUtilities.invokeLater(doFinished);
            } else {
              SwingUtilities.invokeLater(doException);
            }
          }
        };

    Thread t = new Thread(doConstruct);
    threadVar = new ThreadVar(t);
  }

  protected void afterConstruct() {
    // For extending classes to override.
  }

  protected void afterException() {
    // For extending classes to override.
  }

  protected void afterFinished() {
    // For extending classes to override.
  }

  protected void beforeConstruct() {
    // For extending classes to override.
  }

  protected void beforeException() {
    // For extending classes to override.
  }

  protected void beforeFinished() {
    // For extending classes to override.
  }

  /** Compute the value to be returned by the <code>get</code> method. */
  public abstract RESULT construct() throws Exception;

  /**
   * Called on the event dispatching thread (not on the worker thread) after the <code>construct
   * </code> method has thrown an exception.
   */
  public void exception() {
    // This is for classes to override
  }

  /**
   * Called on the event dispatching thread (not on the worker thread) after the <code>construct
   * </code> method has returned normally.
   */
  public void finished() {
    // This is for classes to override
  }

  /**
   * Return the value created by the <code>construct</code> method. Returns null if either the
   * constructing thread or the current thread was interrupted before a value was produced.
   *
   * @return the value created by the <code>construct</code> method
   */
  public RESULT get() {
    while (true) {
      Thread t = threadVar.get();
      if (t == null) {
        return getValue();
      }

      try {
        t.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // propagate
        return null;
      }
    }
  }

  public Exception getException() {
    while (true) {
      Thread t = threadVar.get();
      if (t == null) {
        return getActualException();
      }

      try {
        t.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // propagate
        return null;
      }
    }
  }

  protected synchronized Exception getActualException() {
    return exception;
  }

  /** Get the value produced by the worker thread, or null if it hasn't been constructed yet. */
  protected synchronized RESULT getValue() {
    return value;
  }

  /**
   * A new method that interrupts the worker thread. Call this method to force the worker to stop
   * what it's doing.
   */
  public void interrupt() {
    Thread t = threadVar.get();
    if (t != null) {
      t.interrupt();
    }
    threadVar.clear();
  }

  /** Set the value produced by worker thread */
  protected synchronized void setActualException(Exception exception) {
    this.exception = exception;
  }

  /** Set the value produced by worker thread */
  protected synchronized void setValue(RESULT x) {
    value = x;
  }

  /** Start the worker thread. */
  public void start() {
    setValue(null);
    setActualException(null);

    Thread t = threadVar.get();
    if (t != null) {
      t.start();
    }
  }
}
