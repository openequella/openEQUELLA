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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class ExpiringValue<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  private final long timeout;
  private final T value;

  /** Provides a value that never expires */
  public static <U> ExpiringValue<U> expireNever(U value) {
    return new ExpiringValue<>(value, Long.MAX_VALUE) {
      @Override
      public boolean isTimedOut() {
        return false;
      }
    };
  }

  public static <U> ExpiringValue<U> expireAt(U value, long exactTime) {
    return new ExpiringValue<>(value, exactTime);
  }

  public static <U> ExpiringValue<U> expireAfter(U value, long duration, TimeUnit unit) {
    return new ExpiringValue<>(value, System.currentTimeMillis() + unit.toMillis(duration));
  }

  private ExpiringValue(T value, long expiresAtMillisSinceEpoch) {
    this.value = value;
    this.timeout = expiresAtMillisSinceEpoch;
  }

  public boolean isTimedOut() {
    return System.currentTimeMillis() > timeout;
  }

  /** Return the value if it has not expired, otherwise null. */
  public T getValue() {
    return isTimedOut() ? null : value;
  }
}
