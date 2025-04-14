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

package com.dytech.gui.filter;

import java.util.List;

public abstract class FilterModel<T> {
  private List<T> exclusion;

  public FilterModel() {
    super();
  }

  public abstract List<T> search(String pattern);

  public List<T> removeExclusions(List<T> c) {
    if (exclusion != null && exclusion.size() > 0) {
      c.removeAll(exclusion);
    }
    return c;
  }

  /**
   * @return Returns the exclusion.
   */
  public List<T> getExclusion() {
    return exclusion;
  }

  /**
   * @param exclusion The exclusion to set.
   */
  public void setExclusion(List<T> exclusion) {
    this.exclusion = exclusion;
  }
}
