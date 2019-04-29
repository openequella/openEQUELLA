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

public class Pair<FIRST, SECOND> implements Serializable {
  private static final long serialVersionUID = 1;

  public static <U, V> Pair<U, V> pair(U first, V second) {
    return new Pair<U, V>(first, second);
  }

  private FIRST first;
  private SECOND second;

  public Pair() {
    super();
  }

  public Pair(FIRST first, SECOND second) {
    this.first = first;
    this.second = second;
  }

  public FIRST getFirst() {
    return first;
  }

  public void setFirst(FIRST first) {
    this.first = first;
  }

  public SECOND getSecond() {
    return second;
  }

  public void setSecond(SECOND second) {
    this.second = second;
  }

  @Override
  public String toString() {
    return first == null ? "" : first.toString(); // $NON-NLS-1$
  }

  @Override
  public int hashCode() {
    return first.hashCode() + second.hashCode();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object obj) {
    if (this == obj) {
      // Reflexitivity
      return true;
    } else if (obj == null) {
      // Non-null
      return false;
    } else if (this.getClass() != obj.getClass()) {
      // Symmetry
      return false;
    } else {
      return checkFields((Pair) obj);
    }
  }

  public boolean checkFields(Pair<FIRST, SECOND> rhs) {
    return Check.bothNullOrEqual(rhs.getFirst(), getFirst())
        && Check.bothNullOrEqual(rhs.getSecond(), getSecond());
  }
}
