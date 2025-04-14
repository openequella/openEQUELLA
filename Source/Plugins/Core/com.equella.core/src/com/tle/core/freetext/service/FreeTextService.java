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

package com.tle.core.freetext.service;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.core.remoting.MatrixResults;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import it.uniroma3.mat.extendedset.wrappers.LongSet;
import java.util.Collection;
import java.util.List;

public interface FreeTextService {
  void indexAll();

  /**
   * @param <T>
   * @param searchReq
   * @param start
   * @param count Use -1 for all
   * @return
   */
  <T extends FreetextResult> FreetextSearchResults<T> search(
      Search searchReq, int start, int count);

  /**
   * Another search method which allows users to specify if they want to search attachments.
   *
   * @param searchReq A search request.
   * @param start The first record of a search result.
   * @param count The maximum number of results requested, or -1 for all.
   * @param searchAttachments Whether to search attachments.
   * @return Search results matching the specified criteria, with a maximum length of count.
   */
  <T extends FreetextResult> FreetextSearchResults<T> search(
      Search searchReq, int start, int count, boolean searchAttachments);

  /**
   * Search for items matching the provided search requirements from lucene indexes.
   *
   * @param searchRequirements The search requirements.
   * @param offset The offset of the first result to return.
   * @param maxNumber The maximum number of results to return.
   */
  SearchResults<ItemIdKey> searchIds(Search searchRequirements, int offset, int maxNumber);

  LongSet searchIdsBitSet(Search searchreq);

  int totalCount(Collection<String> hashFrom, String where);

  List<ItemIdKey> getKeysForNodeValue(
      String uuid, ItemDefinition itemdef, String node, String value);

  int[] countsFromFilters(Collection<? extends Search> filters);

  List<ItemIdKey> getAutoCompleteTitles(Search request);

  String getAutoCompleteTerm(Search request, String prefix);

  void waitUntilIndexed(ItemIdKey itemIdKey);

  MatrixResults matrixSearch(
      Search searchRequest, List<String> fields, boolean countOnly, boolean searchAttachments);
}
