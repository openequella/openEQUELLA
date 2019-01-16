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

package com.tle.common.filters;

public class OrFilter<T> implements Filter<T> {
  private final Filter<T>[] filters;

  public static <S> OrFilter<S> create(Filter<S>... filters) {
    return new OrFilter<S>(filters);
  }

  public OrFilter(Filter<T>... filters) {
    this.filters = filters;
  }

  @Override
  public boolean include(T t) {
    for (Filter<T> filter : filters) {
      if (filter != null && filter.include(t)) {
        return true;
      }
    }
    return false;
  }
}
