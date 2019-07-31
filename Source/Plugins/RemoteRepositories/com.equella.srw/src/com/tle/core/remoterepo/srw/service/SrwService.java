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

package com.tle.core.remoterepo.srw.service;

import com.tle.beans.entity.FederatedSearch;
import com.tle.common.Pair;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.remoterepo.srw.service.impl.SrwSearchResults;

/**
 * Same service interface and constants serves SRW and SRU (Requests differ in
 * that the former is SOAP and the latter is over HTTP with GET parameters. For
 * the specifications of the SRU http request format, see<br>
 * http://www.loc.gov/standards/sru/specs/search-retrieve.html<br>
 * The URL to send the query request to, plus<br>
 * <table>
 * <tr>
 * <th>param</th>
 * <th>value</th>
 * <th></th>
 * </tr>
 * <tr>
 * <td>version</td>
 * <td>1.1</td>
 * <td>Mandatory(semi-fixed value)</td>
 * </tr>
 * <tr>
 * <td>operation</td>
 * <td>searchRetrieve</td>
 * <td>Mandatory(fixed value)</td>
 * </tr>
 * <tr>
 * <td>query</td>
 * <td>(as per input)</td>
 * <td>Mandatory: a CQL expression (at its simplest, a simple text expression)</td>
 * </tr>
 * <tr>
 * <td>startRecord</td>
 * <td>as required</td>
 * <td>if supplied, must be greater than 0</td>
 * </tr>
 * <tr>
 * <td>maximumRecords</td>
 * <td>as required</td>
 * <td>if supplied, must be greater than 0</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>recordSchema</td>
 * <td>dc, lom etc</td>
 * <td>as supplied at repository config time. Assume dc as default?</td>
 * </tr>
 *
 * @author agibb
 * @author aholland
 */
@SuppressWarnings("nls")
public interface SrwService {
  String SRW_VERSION = "1.1";

  /**
   * first: long form URI, second: short form uri name. IN the case of the long form for MODS, the
   * value here is an abbreviation, actual value will be such as info:srw/schema/1/mods-v3.0 (or
   * 3.1, 3.2, 3.3), hence String.startsWith is required, not String.equals when comparing.
   */
  Pair<String, String> MARCXML =
      new Pair<String, String>("info:srw/schema/1/marcxml-v1.1", "marcxml");

  Pair<String, String> DC = new Pair<String, String>("info:srw/schema/1/dc-v1.1", "dc");
  Pair<String, String> LOM = new Pair<String, String>("http://ltsc.ieee.org/xsd/LOMv1p0", "lom");
  Pair<String, String> TLE =
      new Pair<String, String>("http://www.thelearningedge.com.au/xsd/item", "tle");
  Pair<String, String> MODS = new Pair<String, String>("info:srw/schema/1/mods-v3", "mods");

  SrwSearchResults search(FederatedSearch srwSearch, String query, int offset, int perpage);

  GenericRecord getRecord(FederatedSearch srwSearch, String qs, int index);
}
