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

package com.tle.core.dao.helpers;

import java.util.Optional;

public final class Pagination {
  private Integer offset;
  private Integer limit;

  private Pagination() {}

  public Optional<Integer> getOffset() {
    return Optional.ofNullable(offset);
  }

  public Optional<Integer> getLimit() {
    return Optional.ofNullable(limit);
  }

  public static Pagination of(Integer offset, Integer limit) {
    Pagination paginationDetails = new Pagination();
    paginationDetails.offset = offset;
    paginationDetails.limit = limit;

    return paginationDetails;
  }

  /**
   * Supports the legacy method of specifying no limits on offset and limit by using negative
   * values.
   *
   * @param offset The offset (-1 for no offset)
   * @param limit The limit (-1 for no limit)
   * @return The pagination details
   */
  public static Pagination ofLegacy(int offset, int limit) {
    return of(convertLegacy(offset), convertLegacy(limit));
  }

  private static Integer convertLegacy(int value) {
    return value < 0 ? null : value;
  }
}
