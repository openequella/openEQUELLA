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

package com.tle.mycontent.soap;

/**
 * @author Aaron
 */
public interface ScrapbookSoapService
{
	/**
	 * Searches the current user's scrapbook content. The XML returned is the
	 * same format as the searchItems method on
	 * {@link com.tle.web.remoting.soap.SoapService41} Additionally each item
	 * has a node for:
	 * <table>
	 * <tr>
	 * <td>content_type</td>
	 * <td>Either mypages or myresource</td>
	 * </tr>
	 * <tr>
	 * <td>keywords</td>
	 * <td>Tags/keywords about the item</td>
	 * </tr>
	 * </table>
	 * 
	 * @param query A freetext query to search over the title and keywords
	 *            properties.
	 * @param resourceTypes Current options are "mypages" or "myresource". Set
	 *            as null for all.
	 * @param mimeTypes If "myresource" is selected you can filter the result by
	 *            MIME type e.g. image/jpeg. Set as null for all.
	 * @param sortOrder Can be one of: 0 - Ranking (search relevance), 1 - Date
	 *            Modified, 2 - Name
	 * @param offset The offset into the resultset you want to get results for.
	 * @param length The max number of results returned with this query. Note
	 *            that if you specify -1 the number of results will be
	 *            unlimited.
	 * @return XML of the search results in the form of: <div class="block">
	 * 
	 *         <pre>
	 * &lt;results count=&quot;number of returned results&quot;&gt;
	 *    &lt;available&gt;number of total results obtainable&lt;/available&gt;
	 *    &lt;result&gt;
	 *       Item XML
	 *    &lt;/result&gt;
	 *    ...
	 * &lt;/results&gt;
	 * </pre>
	 * 
	 *         </div> See the section on XML Formats in
	 *         {@link com.tle.web.remoting.soap SOAP Services} for the format of
	 *         the returned Item XML.
	 */
	String search(String query, String[] resourceTypes, String[] mimeTypes, int sortOrder, int offset, int length);

	/**
	 * Creates a new scrapbook item.
	 * 
	 * @param title The title of the item. Optional: if not supplied the
	 *            filename will be used
	 * @param keywords A comma seperated list of tags/keywords to associate with
	 *            the item. Optional
	 * @param resourceType Must be one of "mypages" or "myresource". Mandatory
	 * @param filename The filename of the attachment. Mandatory
	 * @param base64Data The data for the file. Mandatory
	 * @return The XML for the created item. See the section on XML Formats in
	 *         {@link com.tle.web.remoting.soap SOAP Services}
	 */
	String create(String title, String keywords, String resourceType, String filename, String base64Data);

	/**
	 * Updates a current scrapbook item. Note: if you specify a filename and no
	 * base64data then the current attachment will be renamed.
	 * 
	 * @param itemUuid The uuid of the item to update. Mandatory
	 * @param title The title of the item. Optional
	 * @param keywords A comma seperated list of tags/keywords to associate with
	 *            the item. Optional
	 * @param filename Optional: if not supplied the current filename will be
	 *            used.
	 * @param base64Data Optional: if not supplied the current attachment data
	 *            will be used.
	 * @return The XML for the created item. See the section on XML Formats in
	 *         {@link com.tle.web.remoting.soap SOAP Services}
	 */
	String update(String itemUuid, String title, String keywords, String filename, String base64Data);

	/**
	 * Determine if scrapbook item with UUID of itemUuid exists
	 * 
	 * @param itemUuid The uuid of the item to find
	 * @return true if there is an existing item with this itemUuid.
	 */
	boolean exists(String itemUuid);
}
