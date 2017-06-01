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

package com.tle.core.remoterepo.sru.service;

import com.tle.beans.entity.FederatedSearch;
import com.tle.common.Pair;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.remoterepo.sru.service.impl.SruSearchResults;

/**
 * Same service interface serves SRW and SRU (REquests differ in that the former
 * is SOAP and the latter is over HTTP with GET parameters. For the
 * specifications of the http request format, see<br>
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
 * @author larry
 */
public interface SruService
{
	/**
	 * For SRU http request, version, operation and query are mandatory URL GET
	 * parameters
	 */
	String SRU_VERSION = "version";
	String SRU_VERSION_1_1 = "1.1";
	String SRU_OPERATION = "operation";
	String SRU_SEARCH_RETRIEVE = "searchRetrieve";
	String SRU_QUERY = "query";

	/**
	 * For SRU http request, optional URL GET parameters
	 */
	String SRU_START_RECORD = "startRecord";
	String SRU_MAXIMUM_RECORDS = "maximumRecords";
	String SRU_RECORD_SCHEMA = "recordSchema";

	Pair<String, String> MARCXML = new Pair<String, String>("info:srw/schema/1/marcxml-v1.1", "marcxml");
	Pair<String, String> DC = new Pair<String, String>("info:srw/schema/1/dc-v1.1", "dc");
	Pair<String, String> LOM = new Pair<String, String>("http://ltsc.ieee.org/xsd/LOMv1p0", "lom");
	Pair<String, String> TLE = new Pair<String, String>("http://www.thelearningedge.com.au/xsd/item", "tle");
	Pair<String, String> MODS = new Pair<String, String>("info:srw/schema/1/mods-v3", "mods");

	/**
	 * @param sruSearch
	 * @param query
	 * @param offset
	 * @param perpage
	 * @return
	 */
	SruSearchResults search(FederatedSearch sruSearch, String query, int offset, int perpage);

	GenericRecord getRecord(FederatedSearch sruSearch, String qs, int index);
}
