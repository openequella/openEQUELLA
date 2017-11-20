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

package com.tle.core.remoting;

import com.dytech.edge.common.valuebean.ItemKey;

/**
 * Version 1 of our general purpose Soap Interface. <br>
 * <strong>Important</strong> Sessions: Soap Sessions are now maintained by
 * using HTTP Cookies, so your client must support them in order to work with
 * SoapInterfaceV1. <br>
 * The session id you pass to each method will NOT be used to link to an
 * existing session, it will be used purely for diagnostic purposes. That is, if
 * the session id does not match the session retrieved by the server based on
 * the cookie the client sent (if indeed it did), then an exception will be
 * thrown.
 */

public interface SoapInterfaceV1
{
	/**
	 * Login with the given username and password.<br>
	 * Note that the response sent back by this method will include a new
	 * cookie. Your client MUST have cookies enabled. This method will throw an
	 * exception if Authentication fails.
	 * 
	 * @param username The username
	 * @param password The password
	 * @return A session id that can be used in subsequent method calls for
	 *         diagnostic purposes.<br>
	 *         <b>PLEASE NOTE:</b><br>
	 *         Your Soap Client must use HTTP Cookies in order to maintain a
	 *         session.
	 */
	String login(String username, String password);

	/**
	 * Login with the given token. The token format is described in the
	 * "LMS Integration Specification" document. Note that the response sent
	 * back by this method will include a new cookie. Your client MUST have
	 * cookies enabled.
	 * 
	 * @param token Token string
	 * @return A session id that can be used in subsequent method calls for
	 *         diagnostic purposes.<br>
	 *         <b>PLEASE NOTE:</b><br>
	 *         Your Soap Client must use HTTP Cookies in order to maintain a
	 *         session.
	 */
	String loginWithToken(String token);

	/**
	 * Logout the current user.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 */
	void logout(String ssid);

	/**
	 * This is a light-weight method that can be invoked periodically to ensure
	 * that the current session does not timeout.
	 */
	void keepAlive();

	/**
	 * Returns XML that can be used as a template for creating a new item using
	 * "stopEdit" or "saveEdit". It will be initialised with a new UUID and a
	 * new staging UUID where attachments can be uploaded to.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param itemdefid The UUID of the collection the item will be contributed
	 *            to.
	 * @return The XML for the item.
	 */
	String newItem(String ssid, String itemdefid);

	/**
	 * Edit an existing item. The item will be locked for editing and
	 * attachments copied to a staging folder if requested by bModifyAttach.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param uuid The UUID of the item.
	 * @param version The version of the item.
	 * @param itemdefid This parameter can be ignored since EQUELLA 4.0
	 * @param bModifyAttach Whether or not you want to edit the attached files
	 *            for this item.
	 * @return The XML for the item that you can edit.
	 */
	String startEdit(String ssid, String uuid, int version, String itemdefid, boolean bModifyAttach);

	/**
	 * Stop editing an item and save changes made to an item also unlocks the
	 * item. Before calling this, you should either use "startEdit" or
	 * "newItem".
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param itemXML The item's XML.
	 * @param bSubmit Whether or not to submit the item for moderation.
	 */
	String stopEdit(String ssid, String itemXML, boolean bSubmit);

	/**
	 * Cancel the editing of a previous "startEdit" call. This will unlock the
	 * item.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param uuid The uuid of the item.
	 * @param version The version number of the item.
	 * @param itemdefid This parameter can be ignored since EQUELLA 4.0
	 */
	void cancelEdit(String ssid, String uuid, int version, String itemdefid);

	/**
	 * Force the unlocking of an item (must be the owner of the lock).
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param uuid The uuid of the item.
	 * @param version The version number of the item.
	 * @param itemdefid This parameter can be ignored since EQUELLA 4.0
	 */
	void forceUnlock(String ssid, String uuid, int version, String itemdefid);

	/**
	 * Delete an item.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param uuid the uuid of the item.
	 * @param version the version number of the item.
	 * @param itemdefUuid This parameter can be ignored since EQUELLA 4.0
	 */
	void deleteItem(String ssid, String uuid, int version, String itemdefUuid);

	/**
	 * Upload a file into the staging area of the repository.
	 * 
	 * @param ssid This parameter can be ignored since EQUELLA 3.2
	 * @param stagingid uuid of the staging area.
	 * @param filename the name of the file to upload the data to.
	 * @param data file bytes as a base64 encoded String.
	 */
	void uploadAttachment(String ssid, String stagingid, String filename, String data, boolean overwrite)
		throws Exception;

	/**
	 * Deletes an attachment from a staging area.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param stagingid uuid of the staging area
	 * @param fileName the name of the file to delete from the staging area.
	 */
	void deleteAttachment(String ssid, String stagingid, String fileName);

	/**
	 * Unzip a zip file that has been uploaded to the staging area.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param uuid the UUID of the staging area
	 * @param zipfile the filename of the zip file
	 * @param outpath the directory the zip contents should be output to
	 */
	void unzipFile(String ssid, String uuid, String zipfile, String outpath);

	/**
	 * Search items using an xml description of the Search Request parameters.
	 * <code><pre>
	 * &lt;com.dytech.edge.common.valuebean.SearchRequest&gt;
	 *  &lt;query&gt;search text&lt;/query&gt;
	 *  &lt;select&gt;*&lt;/select&gt;
	 *  &lt;where&gt;where /xml/item/name like 'hello%'&lt;/where&gt;
	 *  &lt;orderby&gt;not used&lt;/orderby&gt;
	 *  &lt;onlyLive&gt;false&lt;/onlyLive&gt;
	 *  &lt;orderType&gt;2&lt;/orderType&gt;
	 *  &lt;sortReverse&gt;false&lt;/sortReverse&gt;
	 *  &lt;itemdefs class=&quot;list&quot;&gt;
	 *   &lt;string&gt;uuid1&lt;/string&gt;
	 *   &lt;string&gt;uuid2&lt;/string&gt;
	 *   &lt;string&gt;uuid3&lt;/string&gt;
	 *  &lt;/itemdefs&gt;
	 * &lt;/com.dytech.edge.common.valuebean.SearchRequest&gt;
	 * </code></pre> If you leave a tag out, it will be assumed to be the
	 * default (usually null, 0 or false)<br>
	 * <br>
	 * select: Must be *<br>
	 * If any other value is specified, only the system item metadata will be
	 * returned.<br>
	 * <br>
	 * where: The BNF for the where clause is<br>
	 * <code><pre>
	 * 	      WHERE STATEMENT ::= "where"? BOOLEAN_EXPR
	 * 			 BOOLEAN_EXPR ::= OR_BOOLEAN_EXPR
	 * 		  OR_BOOLEAN_EXPR ::= AND_BOOLEAN_EXPR ("or" AND_BOOLEAN_EXPR)*
	 * 		 AND_BOOLEAN_EXPR ::= CLAUSE ("and" CLAUSE)*
	 * 				   CLAUSE ::= "not" CLAUSE | BRACKETS | COMPARISON | EXISTS_CLAUSE
	 * 	             BRACKETS ::= "(" BOOLEAN_EXPR ")"
	 * 		       COMPARISON ::= XPATH COMPARISON_OP COMPARISON_RHS
	 * 			EXISTS_CLAUSE ::= XPATH "exists"
	 * 	                XPATH ::= "/" (ALPHA | NUMBER | [/._:@])+
	 * 			COMPARISON_OP ::= "=" | "is" | "&lt;>" | "is not" | "&lt;" | "&lt;=" | ">" | ">=" |
	 * 							  "like" | "not like" | "in" | "not in"
	 * 		   COMPARISON_RHS ::= "null" | NUMBER_VALUE | STRING_VALUE | GROUP_VALUE
	 * 	         STRING_VALUE ::= "'" STRING "'"
	 * 			 NUMBER_VALUE ::= NUMBER+
	 * 	          GROUP_VALUE ::= "(" STRING_VALUE ("," STRING_VALUE)* ")"
	 *                 STRING ::= (ALPHA | [0-9] | ...)*
	 *                  ALPHA ::= [a-zA-Z]
	 *                 NUMBER ::= [0-9]</code></pre> <br>
	 * orderType: can be 0 - ranking, 1 - Date Modified, 2 - Name<br>
	 * <br>
	 * To not use a freetext query, (e.g. return all items that the where
	 * matches) just leave out the <code>&lt;query></code> tag <br>
	 * <br>
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param searchReqStr A string representing the XML for the SearchRequest
	 * @param offset The offset into the resultset you want to get results for.
	 * @param limit The max number of results returned with this query.
	 * @return xml search results in the form of <br>
	 *         &lt;results> <br>
	 *         &lt;result> <br>
	 *         ..<br>
	 *         &lt;/result> <br>
	 *         &lt;result> <br>
	 *         ... <br>
	 *         &lt;/result> <br>
	 *         &lt;/results> <br>
	 */
	String searchItems(String ssid, String searchReqStr, int offset, int limit);

	/**
	 * Counts the number of items for the given collection definition and where
	 * clause.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param itemdefs An array of collection definition UUIDs.
	 * @param where A where clause. Can be blank.
	 * @return The number of items that were counted.
	 */
	int queryCount(String ssid, String[] itemdefs, String where);

	/**
	 * Retrieves the XML for an item.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param itemUuid UUID of the item to retrieve.
	 * @param itemdef This parameter can be ignored since EQUELLA 4.0
	 * @param version The version of the item to retrieve. A version of '0' will
	 *            retrieve the latest version.
	 * @param select An XOQL select clause for choosing which parts of the item
	 *            to retrieve. <code>null</code> or an empty string is
	 *            equivalent to &quot;*&quot;
	 * @return A string representation of the item XML.
	 */
	String getItem(String ssid, String itemUuid, int version, String itemdef, String select);

	/**
	 * Return an xml enumeration of all writable itemdef's of the form: &lt;xml>
	 * &lt;itemdef> &lt;uuid/> - the itemdef's uuid &lt;name/> - the itemdef
	 * name &lt;type/> - the itemdef type (e.g. generic) &lt;contuuid/> - the
	 * container uuid &lt;table/> - the itemdef's table &lt;writable/> -
	 * true/false &lt;searchable/> - true/false whether the itemdef is
	 * searchable &lt;expert/> true/false whether or not the itemdef has an
	 * expert search &lt;identifier/> the itemdef identifer (e.g basres)
	 * &lt;/itemdef> ... &lt;/xml>
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @return The xml for the enumeration
	 */
	String enumerateWritableItemDefs(String ssid);

	/**
	 * Enumerates the dependencies of items.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param items An array of ItemKey's to calculate dependencies for
	 * @param recurse Recursively find dependencies for the items
	 * @return An array of ItemKey dependencies for each item specified in the
	 *         input array
	 */
	ItemKey[][] enumerateItemDependencies(String ssid, ItemKey[] items, boolean recurse);
}
