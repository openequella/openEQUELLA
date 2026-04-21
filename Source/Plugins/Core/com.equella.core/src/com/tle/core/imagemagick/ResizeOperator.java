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

package com.tle.core.imagemagick;

/** Resize geometry operators appended to ImageMagick dimension strings. */
public enum ResizeOperator {
  /** Shrink only: resize down if larger than target, never enlarge. */
  SHRINK_ONLY(">"),
  /** Fill area: resize to completely cover the target dimensions (may exceed on one axis). */
  FILL_AREA("^"),
  /** Exact: force the exact dimensions, ignoring aspect ratio. */
  EXACT("!"),
  /** Default: no special operator, standard resize behaviour. */
  DEFAULT("");

  private final String operator;

  ResizeOperator(String operator) {
    this.operator = operator;
  }

  public String getOperator() {
    return operator;
  }
}
