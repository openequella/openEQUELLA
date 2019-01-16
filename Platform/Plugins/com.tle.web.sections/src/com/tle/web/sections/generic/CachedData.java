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

package com.tle.web.sections.generic;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public class CachedData<T> {
  private boolean cached;
  private T cache;

  @Nullable
  public T getCachedValue() {
    if (!cached) {
      throw new RuntimeException("Not cached yet"); // $NON-NLS-1$
    }
    return cache;
  }

  @Nullable
  public T get(SectionInfo info, CacheFiller<T> filler) {
    if (!cached) {
      cache = filler.get(info);
      cached = true;
    }
    return cache;
  }

  @NonNullByDefault
  public interface CacheFiller<T> {
    @Nullable
    T get(SectionInfo info);
  }

  public void clear() {
    cached = false;
  }
}
