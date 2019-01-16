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

import java.util.Comparator;

public final class NumberOrderComparator implements Comparator<NumberOrder> {
  public static final NumberOrderComparator HIGHEST_FIRST = new NumberOrderComparator(false);
  public static final NumberOrderComparator LOWEST_FIRST = new NumberOrderComparator(true);

  private final int orderNum;

  private NumberOrderComparator(boolean reverse) {
    orderNum = reverse ? -1 : 1;
  }

  @Override
  public int compare(NumberOrder o1, NumberOrder o2) {
    int order = o1.getOrder();
    int order2 = o2.getOrder();
    if (order == order2) {
      return 0;
    } else {
      return order < order2 ? orderNum : -orderNum;
    }
  }
}
