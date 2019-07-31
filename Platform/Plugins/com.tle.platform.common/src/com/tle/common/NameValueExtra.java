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

/**
 * Rather than make the NameValue parent a Comparable and risk unintended consequences in any of the
 * many places where that class is utilised, we'll adopt this class as our comparable-by-name
 * NameValue variant.
 *
 * @author larry
 */
public class NameValueExtra extends NameValue implements Comparable<NameValueExtra> {
  /** */
  private static final long serialVersionUID = -6675973016715913685L;

  private String extra;

  public NameValueExtra(String name, String value, String extra) {
    super(name, value);
    this.extra = extra;
  }

  public String getExtra() {
    return extra;
  }

  @Override
  public boolean checkFields(Pair<String, String> rhs) {
    NameValueExtra t = (NameValueExtra) rhs;
    return super.checkFields(t) && Check.bothNullOrEqual(t.getExtra(), getExtra());
  }

  /** compare name, value, extra in that order until we get a non-0 result */
  @Override
  public int compareTo(NameValueExtra o) {
    int strCompare = 0;

    if (o == null) {
      return 1;
    }
    if (this.getFirst() == null) {
      return -1;
    } else {
      strCompare = this.getFirst().compareToIgnoreCase(o.getFirst());
      if (strCompare == 0 && this.getSecond() != null) {
        strCompare = this.getSecond().compareToIgnoreCase(o.getSecond());
        if (strCompare == 0 && this.getExtra() != null) {
          strCompare = this.getExtra().compareToIgnoreCase(o.getExtra());
        }
      }
    }
    return strCompare;
  }
}
