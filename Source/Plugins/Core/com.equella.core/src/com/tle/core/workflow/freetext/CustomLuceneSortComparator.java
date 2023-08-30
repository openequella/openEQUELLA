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

package com.tle.core.workflow.freetext;

import com.tle.common.Check;
import java.io.IOException;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.LeafFieldComparator;

public final class CustomLuceneSortComparator extends FieldComparator<Integer> {

  private final String userId;
  private int[] values;

  private int numHits;
  private String[] currentReaderValues;
  private final String field;
  private int bottom;
  private int top;

  public CustomLuceneSortComparator(int numHits, String field, String userId) {
    values = new int[numHits];
    this.numHits = numHits;
    this.field = field;
    this.userId = userId;
  }

  @Override
  public int compare(int slot1, int slot2) {
    final int val1 = values[slot1];
    final int val2 = values[slot2];
    return val1 - val2;
  }

  public int isCurrentUser(String userId) {
    return this.userId.equals(userId) ? 0 : Check.isEmpty(userId) ? 1 : 2;
  }

  //  @Override
  //  public FieldComparator<Integer> setNextReader(LeafReaderContext context) throws IOException {
  //    int maxDoc = context.reader().maxDoc();
  //    currentReaderValues = new String[maxDoc];
  //
  //    BinaryDocValues docValues = FieldCache.DEFAULT.getTerms(context.reader(), field, true);
  //    for (int i = 0; i < maxDoc; i++) {
  //      BytesRef value = docValues.get(i);
  //      currentReaderValues[i] = value == null ? "" : value.utf8ToString();
  //    }
  //
  //    return this;
  //  }

  @Override
  public void setTopValue(Integer top) {
    this.top = top;
  }

  @Override
  public Integer value(int slot) {
    return values[slot];
  }

  @Override
  public LeafFieldComparator getLeafComparator(LeafReaderContext context) throws IOException {
    return new IntComparator(numHits, field, 0);
  }

  @Override
  public int compareValues(Integer val1, Integer val2) {
    return val1 - val2;
  }
}
