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

package com.tle.common.scripting.objects;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.CollectionScriptType;
import com.tle.common.scripting.types.ConnectionScriptType;
import com.tle.common.scripting.types.FacetCountResultScriptType;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.common.scripting.types.SearchResultsScriptType;
import com.tle.common.scripting.types.XmlScriptType;

/**
 * Referenced by the 'utils' variable in script
 */
public interface UtilsScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "utils"; //$NON-NLS-1$

	/**
	 * Search for items.
	 * 
	 * @param query A freetext query
	 * @param offset First result index
	 * @param maxResults Number of results to get
	 * @return A SearchResultsScriptType object
	 */
	SearchResultsScriptType search(String query, int offset, int maxResults);

	/**
	 * Search for items with more advanced settings.
	 * 
	 * @param query A freetext query
	 * @param where An xpath/SQL like where clause. E.g.: /xml/mynode like
	 *            'something%'
	 * @param onlyLive Return only items with a status of Live (e.g. not Draft)
	 * @param orderType Can be one of: 0 - Ranking (search relevance), 1 - Date
	 *            Modified, 2 - Name
	 * @param reverseOrder Specify true to reverse the order specified in
	 *            orderType
	 * @param offset First result index
	 * @param maxResults Number of results to get
	 * @return A SearchResultsScriptType object
	 */
	SearchResultsScriptType searchAdvanced(String query, String where, boolean onlyLive, int orderType,
		boolean reverseOrder, int offset, int maxResults);

	/**
	 * Counts the number of items for the given collection definition and where
	 * clause.
	 * 
	 * @param collectionUuids An array of collection definition UUIDs.
	 * @param where A where clause. Can be blank. See
	 *            <code>searchAdvanced</code> for the format of the where
	 *            clause.
	 * @return The number of items that were counted.
	 */
	int queryCount(String[] collectionUuids, String where) throws Exception;

	/**
	 * Retrieves a list of terms and item counts for an XPath.
	 * 
	 * @param query A freetext query
	 * @param collectionUuids An array of collection definition UUIDs.
	 * @param where An xpath/SQL like where clause. E.g.: /xml/mynode like
	 *            'something%'
	 * @param facetXpath the XPath of the facet to return the terms and counts
	 *            for.
	 * @return A list of FacetCountResultScriptType objects sorted by highest to
	 *         lowest frequency.
	 */
	List<FacetCountResultScriptType> facetCount(String query, String[] collectionUuids, String where, String facetXpath);

	/**
	 * Return a CollectionScriptType object based on the collectionUuid.
	 * 
	 * @param collectionUuid The UUID of the collection to locate
	 * @return A CollectionScriptType object or null if not found
	 */
	CollectionScriptType getCollectionFromUuid(String collectionUuid);

	/**
	 * Get a Java date from a string.
	 * 
	 * @param date A date in string format
	 * @param format The format of the date supplied
	 * @return A java.util.Date
	 * @throws ParseException If the format is invalid
	 */
	Date getDate(String date, String format) throws ParseException;

	/**
	 * Determine if the text has any non-whitespace value
	 * 
	 * @param text The value to check
	 * @return true if the value is null or an empty or all-whitespace string
	 */
	boolean isEmpty(String text);

	/**
	 * Starts a connection with the site at URL.
	 * 
	 * @param url The URL to connect to
	 * @return A script object for retrieving data from the connection
	 */
	ConnectionScriptType getConnection(String url);

	/**
	 * Throws an exception. This will prevent any operation from completing
	 * normally, e.g. a wizard page submit, or an item save. You should only use
	 * this method in critical situations (e.g. an item cannot be saved)
	 * 
	 * @param message The error message
	 */
	void throwError(String message);

	/**
	 * @param item An item to calculate the URL of
	 * @return A full URL to this item. E.g.
	 *         http://myinstitution.edu.au/items/323a213e21fd31e2/1
	 */
	String getItemUrl(ItemScriptType item);

	/**
	 * Creates a new XML document.
	 * 
	 * @return A new XML document.
	 */
	XmlScriptType newXmlDocument();

	/**
	 * Creates a new XML document populated with the given XML string.
	 * 
	 * @param xmlString XML string to populate the document with.
	 * @return A new XML document.
	 */
	XmlScriptType newXmlDocumentFromString(String xmlString);
}
