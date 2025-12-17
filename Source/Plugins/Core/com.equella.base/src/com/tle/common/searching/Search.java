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

package com.tle.common.searching;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.ItemSelect;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface Search {
  String INDEX_TASK = "task";
  String INDEX_ITEM = "item";
  String INDEX_SCRIPT_TASK = "scripttask";

  public enum SortType {
    RANK(null, false),
    DATEMODIFIED(FreeTextQuery.FIELD_REALLASTMODIFIED, true),
    DATECREATED(FreeTextQuery.FIELD_REALCREATED, true),
    NAME(FreeTextQuery.FIELD_NAME, false),
    FORCOUNT(null, false),
    RATING(FreeTextQuery.FIELD_RATING, true),
    ADDEDAT(FreeTextQuery.FIELD_ADDED_AT, true);

    private final String field;
    private final boolean reverse;

    private SortType(String field, boolean reverse) {
      this.field = field;
      this.reverse = reverse;
    }

    public SortField getSortField() {
      return getSortField(false);
    }

    public SortField getSortField(boolean reverseTheDefault) {
      boolean r = reverseTheDefault ? !reverse : reverse;
      return new SortField(field, r, field == null ? SortField.Type.SCORE : SortField.Type.STRING);
    }
  }

  ItemSelect getSelect();

  FreeTextQuery getFreeTextQuery();

  String getQuery();

  List<String> getExtraQueries();

  Collection<String> getTokenisedQuery();

  SortField[] getSortFields();

  boolean isSortReversed();

  Date[] getDateRange();

  String getSearchType();

  List<Field> getMatrixFields();

  List<List<Field>> getMust();

  List<List<Field>> getMustNot();

  String getPrivilege();

  String getPrivilegePrefix();

  String getPrivilegeToCollect();

  Collection<DateFilter> getDateFilters();

  /**
   * Indicates if the server time zone should be included in searches.
   *
   * @return True if server time zone is used or otherwise false.
   */
  boolean useServerTimeZone();
}
