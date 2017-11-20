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

package com.tle.web.entity.services;

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * General purpose SOAP interface allowing item creation, searching, and user
 * management.
 * <p>
 * The WSDL file for this service is available at
 * http://INSTITUTION_URL/services/SoapService41?wsdl
 * <p>
 * Additional methods added in the future will be added to new interfaces
 * extending this interface. (Functionality will never be changed or removed)
 * <p>
 * Simplified example method usage:<br>
 * <div class="block">
 * 
 * <pre>
 * soap.login(&quot;username&quot;, &quot;password&quot;);
 * xml = soap.newItem(&quot;a collection UUID&quot;);
 * manipulateNewItem(xml);
 * soap.saveItem(xml, true);
 * soap.logout();
 * </pre>
 * 
 * </div>
 */
@WebService
public interface SoapService41
{
	/**
	 * Starts a session with the EQUELLA server. The permissions granted to you
	 * are completely dependent on the user you login as. That is, you cannot do
	 * anything that you would not normally be able to do by logging into the
	 * EQUELLA Resource Centre itself.
	 * <p>
	 * Note that the response sent back by this method will include a new
	 * cookie. Your client MUST have cookies enabled.
	 * 
	 * @param username The account name of the user to login as
	 * @param password The password (plain text) for the user's account
	 * @return The user XML. See the section on XML Formats in
	 *         {@link com.tle.web.remoting.soap SOAP Services}
	 * @throws An exception is thrown if login fails for any reason (e.g.
	 *             invalid credentials)
	 */
	// FIXME: these WebParams can actually currently be used, however legacy
	// clients will break.
	// If you can get the ParamTransformInInterceptor to work with nice param
	// names as well then
	// be my guest :)
	String login(@WebParam(name = "in0") String username, @WebParam(name = "in1") String password);

	/**
	 * Login with the given token. The token format is described in the
	 * "LMS Integration Specification" document.
	 * <p>
	 * Note that the response sent back by this method will include a new
	 * cookie. Your client MUST have cookies enabled.
	 * 
	 * @param token Token string
	 * @return The user XML. See the section on XML Formats in
	 *         {@link com.tle.web.remoting.soap SOAP Services}
	 * @throws An exception is thrown if login fails for any reason (e.g.
	 *             invalid credentials)
	 */
	String loginWithToken(@WebParam(name = "in0") String token);

	/**
	 * Stops the currently logged in user's session on the server. After calling
	 * Logout, you must call Login again to use any other methods on the
	 * interface.
	 */
	void logout();

	/**
	 * This is a light-weight method that can be invoked periodically to ensure
	 * that the current session does not timeout.
	 */
	void keepAlive();

	/**
	 * Creates an item in the collection with the id of collectionId for you to
	 * begin editing with. Note that this will not create an item on the server
	 * until you call the saveItem method. The item XML will be initialised with
	 * a new UUID and a new staging ID where attachments can be uploaded to.
	 * 
	 * @param collectionUuid The UUID of the collection the item will be
	 *            contributed to.
	 * @return The XML for the new item that you can edit. See the section on
	 *         XML Formats in {@link com.tle.web.remoting.soap SOAP Services}
	 */
	String newItem(@WebParam(name = "in0") String collectionUuid);

	/**
	 * Edit an existing item. The item will be locked for editing and
	 * attachments copied to a staging folder if requested by the
	 * modifyingAttachments parameter. You must call saveItem to commit your
	 * changes.
	 * 
	 * @param itemUuid The UUID of the item.
	 * @param itemVersion The version of the item.
	 * @param modifyingAttachments Specify true if you want to edit the attached
	 *            files for this item. If false is specified a performance
	 *            improvement is gained as the attachments are not copied to the
	 *            staging area.
	 * @return The XML for the item that you can edit. See the section on XML
	 *         Formats in {@link com.tle.web.remoting.soap SOAP Services}
	 */
	String editItem(String itemUuid, int itemVersion, boolean modifyingAttachments);

	/**
	 * Save changes made to an item which also unlocks the item. Before calling
	 * this, you must either use {@link #editItem(String, int, boolean)
	 * editItem} or {@link #newItem(String) newItem} and use the XML returned by
	 * these methods to pass in as the itemXML parameter.
	 * 
	 * @param itemXML The item's XML retrieved from
	 *            {@link #editItem(String, int, boolean) editItem} or
	 *            {@link #newItem(String) newItem}, plus any of your
	 *            modifications.
	 * @param submit If true, submit the item for moderation. If false, the item
	 *            will stay in draft status.
	 */
	String saveItem(String itemXML, boolean submit);

	/**
	 * Creates a new version of an existing item. The item will be locked for
	 * editing and attachments copied to a staging folder if requested by the
	 * modifyingAttachments parameter. You must call saveItem to commit your
	 * changes.
	 * 
	 * @param itemUuid The UUID of the original item to create a new version of.
	 * @param itemVersion The version of the original item to create a new
	 *            version of.
	 * @param copyAttachments Include the attachments of the original item with
	 *            the new version
	 * @return The XML for the item that you can edit. See the section on XML
	 *         Formats in {@link com.tle.web.remoting.soap SOAP Services}
	 */
	String newVersionItem(String itemUuid, int itemVersion, boolean copyAttachments);

	/**
	 * Creates a clone of an existing item. The item will be locked for editing
	 * and attachments copied to a staging folder if requested by the
	 * modifyingAttachments parameter. You must call saveItem to commit your
	 * changes.
	 * 
	 * @param itemUuid The UUID of the original item to create a clone of.
	 * @param itemVersion The version of the original item to create a clone of.
	 * @param copyAttachments Include the attachments of the original item with
	 *            the new version.
	 * @return The XML for the item that you can edit. See the section on XML
	 *         Formats in {@link com.tle.web.remoting.soap SOAP Services}
	 */
	String cloneItem(String itemUuid, int itemVersion, boolean copyAttachments);

	/**
	 * Archives an item. Will not delete the item if it is already archived.
	 * 
	 * @param itemUuid The UUID of the item to archive.
	 * @param itemVersion The version of the item to archive.
	 */
	void archiveItem(String itemUuid, int itemVersion);

	/**
	 * Cancel the editing of a previous {@link #editItem(String, int, boolean)
	 * editItem} or {@link #newItem(String) newItem} call. This will unlock the
	 * item and remove any temporary files.
	 * 
	 * @param itemUuid The UUID of the item.
	 * @param itemVersion The version of the item.
	 */
	void cancelItemEdit(String itemUuid, int itemVersion);

	/**
	 * Force the unlocking of an item (must be the owner of the lock).
	 * 
	 * @param itemUuid The UUID of the item.
	 * @param itemVersion The version of the item.
	 */
	void unlock(String itemUuid, int itemVersion);

	/**
	 * Delete an item. This will set the status of the item to 'deleted' OR it
	 * will purge the item from the system completely if the item has already
	 * been set to 'deleted'
	 * 
	 * @param itemUuid the uuid of the item.
	 * @param itemVersion the version of the item.
	 */
	void deleteItem(String itemUuid, int itemVersion);

	/**
	 * Upload a file into the staging area. Note that this does not attach the
	 * file to your item! To link the file to the item you need to add an
	 * attachment node to the item XML. See the section on XML Formats in
	 * {@link com.tle.web.remoting.soap SOAP Services} for the location and
	 * format of the attachment XML.
	 * 
	 * @param stagingId The ID of the staging area. This can be retrieved from
	 *            the XML returned by {@link #newItem(String) newItem} or
	 *            {@link #editItem(String, int, boolean) editItem}.
	 * @param filename the name of the file to upload the data to, relative to
	 *            the item's directory
	 * @param base64Data file bytes as a base64 encoded String.
	 * @param overwrite Replace the current file with the same filename if there
	 *            is any. If you specify false, the existing file will be
	 *            appended to. This is useful (and advisable for large files)
	 *            for uploading files in manageable chunks.
	 */
	void uploadFile(String stagingId, String filename, String base64Data, boolean overwrite);

	/**
	 * Deletes an attachment from a staging area. Note that this does not remove
	 * the link to the file from your item if there is an existing link to the
	 * file! To remove the link to the file on the item (if there is one) you
	 * need to remove the appropriate attachment node from the item XML. See the
	 * section on XML Formats in {@link com.tle.web.remoting.soap SOAP Services}
	 * for the location and format of the attachment XML.
	 * 
	 * @param stagingId The ID of the staging area. This can be retrieved from
	 *            the XML returned by {@link #newItem(String) newItem} or
	 *            {@link #editItem(String, int, boolean) editItem}.
	 * @param filename The name of the file to delete from the staging area,
	 *            relative to the item's directory
	 */
	void deleteFile(String stagingId, String filename);

	/**
	 * Unzip a zip file that has been uploaded to the staging area.
	 * 
	 * @param stagingId The ID of the staging area. This can be retrieved from
	 *            the XML returned by {@link #newItem(String) newItem} or
	 *            {@link #editItem(String, int, boolean) editItem}.
	 * @param zipfile The filename of the zip file, relative to the item's
	 *            directory e.g. "myzips/zipfile.zip"
	 * @param outpath The directory the zip contents should be output to,
	 *            relative to the item's directory e.g. "unzipped/zipfile"
	 */
	void unzipFile(String stagingId, String zipfile, String outpath);

	/**
	 * Search for items that the current user can discover.
	 * 
	 * @param freetext A freetext query. If null is specified, '*' is used
	 * @param collectionUuids An array of collections UUIDs to restrict results
	 *            to. If null or empty, all collections are searched.
	 * @param whereClause The format for the where clause is:<br>
	 *            <div class="block">
	 * 
	 *            <pre>
	 *      WHERE STATEMENT ::= &quot;where&quot;? BOOLEAN_EXPR
	 *         BOOLEAN_EXPR ::= OR_BOOLEAN_EXPR
	 *      OR_BOOLEAN_EXPR ::= AND_BOOLEAN_EXPR (&quot;or&quot; AND_BOOLEAN_EXPR)*
	 *     AND_BOOLEAN_EXPR ::= CLAUSE (&quot;and&quot; CLAUSE)*
	 *               CLAUSE ::= &quot;not&quot; CLAUSE | BRACKETS | COMPARISON | EXISTS_CLAUSE
	 *             BRACKETS ::= &quot;(&quot; BOOLEAN_EXPR &quot;)&quot;
	 *           COMPARISON ::= XPATH COMPARISON_OP COMPARISON_RHS
	 *        EXISTS_CLAUSE ::= XPATH &quot;exists&quot;
	 *                XPATH ::= &quot;/&quot; (ALPHA | NUMBER | [/._:@])+
	 *        COMPARISON_OP ::= &quot;=&quot; | &quot;is&quot; | &quot;&lt;&gt;&quot; | &quot;is not&quot; | &quot;&lt;&quot; | &quot;&lt;=&quot; | &quot;&gt;&quot; | &quot;&gt;=&quot; |
	 *                          &quot;like&quot; | &quot;not like&quot; | &quot;in&quot; | &quot;not in&quot;
	 *       COMPARISON_RHS ::= &quot;null&quot; | NUMBER_VALUE | STRING_VALUE | GROUP_VALUE
	 *         STRING_VALUE ::= &quot;'&quot; STRING &quot;'&quot;
	 *         NUMBER_VALUE ::= NUMBER+
	 *          GROUP_VALUE ::= &quot;(&quot; STRING_VALUE (&quot;,&quot; STRING_VALUE)* &quot;)&quot;
	 *               STRING ::= (ALPHA | [0-9] | ...)*
	 *                ALPHA ::= [a-zA-Z]
	 *               NUMBER ::= [0-9]
	 * </pre>
	 * 
	 *            </div> For example,
	 *            <code>where /xml/item/name not like 'hello%'</code>. Note that
	 *            any schema node you search on in the where clause must have
	 *            "Index for Advanced Searches" selected.
	 * @param onlyLive Return only items with a status of Live (e.g. not Draft)
	 * @param orderType Can be one of: 0 - Ranking (search relevance), 1 - Date
	 *            Modified, 2 - Name, 4 - Rating
	 * @param reverseOrder Specify true to reverse the order specified in
	 *            orderType
	 * @param offset The offset into the resultset you want to get results for.
	 * @param length The max number of results returned with this query. Note
	 *            that if you specify -1 the number of results will be capped at
	 *            50.
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
	String searchItems(String freetext, String[] collectionUuids, String whereClause, boolean onlyLive, int orderType,
		boolean reverseOrder, int offset, int length);

	/**
	 * Similar to searchItems but with two big differences: a) it is much
	 * faster, and b) it does not return results in the standard item XML
	 * format. All parameters are identical to searchItems unless indicated
	 * below.
	 * 
	 * @param length Same as searchItems, but restricted to a max of 50.
	 * @param resultCategories indicates which groups of information should be
	 *            returned for each result. Valid options are 'basic' to return
	 *            the name and description, 'metadata' to return the items
	 *            metadata, or blank to only return the uuid and version.
	 * @return
	 */
	String searchItemsFast(String freetext, String[] collectionUuids, String whereClause, boolean onlyLive,
		int orderType, boolean reverseOrder, int offset, int length, String[] resultCategories);

	/**
	 * Counts the number of items for the given collection definition and where
	 * clause.
	 * 
	 * @param collectionUuids An array of collection definition UUIDs.
	 * @param where A where clause. Can be blank. See searchItems for the format
	 *            of the where clauses.
	 * @return The number of items that were counted.
	 */
	int queryCount(String[] collectionUuids, String where);

	/**
	 * Identical to <code>queryCount</code> but allows for multiple where
	 * clauses to be passed, and an array of respective counts to be returned.
	 * 
	 * @param collectionUuids An array of collection definition UUIDs.
	 * @param wheres An array of where clauses. See searchItems for the format
	 *            of the where clause.
	 * @return The number of items that were counted.
	 */
	int[] queryCounts(String[] collectionUuids, String[] wheres);

	/**
	 * Identical to <code>queryCount</code> but allows for multiple where
	 * clauses to be passed, and an array of respective counts to be returned.
	 * 
	 * @param freetext A freetext query. If null is specified, '*' is used.
	 * @param collectionUuids An array of collection definition UUIDs.
	 * @param where A where clause. See searchItems for the format of the where
	 *            clause.
	 * @param facetXpaths the XPaths of each facet to return a count for.
	 * @return The values and count of matching items for each facet. It is
	 *         returned in the following XML form: <div class="block">
	 * 
	 *         <pre>
	 * &lt;facets&gt;
	 *     &lt;facet xpath=&quot;/xml/item/mediatype&quot;&gt;
	 *         &lt;value count=&quot;52&quot;&gt;animation&lt;/value&gt;
	 *         &lt;value count=&quot;8&quot;&gt;audio&lt;/value&gt;
	 *         &lt;value count=&quot;11&quot;&gt;image&lt;/value&gt;
	 *         ...
	 *     &lt;/facet&gt;
	 *     &lt;facet xpath=&quot;/xml/item/classification/learningarea&quot;&gt;
	 *         ...
	 *     &lt;/facet&gt;
	 *     ...
	 * &lt;/facets&gt;
	 * </pre>
	 * 
	 *         </div>
	 */
	String facetCount(String freetext, String[] collectionUuids, String whereClause, String[] facetXpaths)
		throws Exception;

	/**
	 * Retrieves the XML for an item. Note that the item XML returned from this
	 * method is unfit for editing, that is, you cannot call saveItem on it. You
	 * should call editItem if you wish to edit and save the item XML. See the
	 * section on XML Formats in {@link com.tle.web.remoting.soap SOAP Services}
	 * 
	 * @param itemUuid UUID of the item to retrieve.
	 * @param itemVersion The version of the item to retrieve. A version of 0
	 *            will retrieve the latest version.
	 * @param select Currently not functional
	 * @return A string representation of the item XML. See the section on XML
	 *         Formats in {@link com.tle.web.remoting.soap SOAP Services}
	 */
	String getItem(String itemUuid, int itemVersion, String select);

	/**
	 * Returns a list of all collections that the currently logged in user can
	 * specifically choose for searching. <br>
	 * <div class="block">
	 * 
	 * <pre>
	 * &lt;xml&gt;
	 *   Collection XML
	 *   ...
	 * &lt;/xml&gt;
	 * </pre>
	 * 
	 * </div>
	 * <em>See the section on XML Formats in {@link com.tle.web.remoting.soap SOAP Services} for the
	 * format of the returned Collection XML.</em>
	 * 
	 * @return The xml for the enumeration
	 */
	String getSearchableCollections();

	/**
	 * Returns a list of all collections that the currently logged in user can
	 * contribute to. <br>
	 * <div class="block">
	 * 
	 * <pre>
	 * &lt;xml&gt;
	 *   Collection XML
	 *   ...
	 * &lt;/xml&gt;
	 * </pre>
	 * 
	 * </div>
	 * <em>See the section on XML Formats in {@link com.tle.web.remoting.soap SOAP Services} for the
	 * format of the returned Collection XML.</em>
	 * 
	 * @return The xml for the enumeration
	 */
	String getContributableCollections();

	/**
	 * Re-assigns ownership of an item
	 * 
	 * @param itemUuid The UUID of item to change the owner of
	 * @param itemVersion The version of the item to change to owner of
	 * @param userId The unique ID of the user to make the new owner
	 */
	void setOwner(String itemUuid, int itemVersion, String userId);

	/**
	 * Adds a shared owner to an item
	 * 
	 * @param itemUuid The UUID of item to add a shared owner to
	 * @param itemVersion The version of the item to change to add a shared
	 *            owner to
	 * @param userId The unique ID of the user to add as a shared owner
	 */
	void addSharedOwner(String itemUuid, int itemVersion, String userId);

	/**
	 * Removes a shared owner from an item
	 * 
	 * @param itemUuid The UUID of item to remove a shared owner from
	 * @param itemVersion The version of the item to remove a shared owner from
	 * @param userId The unique ID of the user to remove as a shared owner
	 */
	void removeSharedOwner(String itemUuid, int itemVersion, String userId);

	/**
	 * Accept the moderation task for the given item.
	 * 
	 * @param itemUuid The UUID of the item.
	 * @param itemVersion The version of the item.
	 * @param taskId The UUID of the task to accept.
	 * @param unlock Whether or not to unlock the item after accepting.
	 * @return The unique ID of the task that was accepted.
	 * @throws Exception
	 */
	String acceptTask(String itemUuid, int itemVersion, String taskId, boolean unlock);

	/**
	 * Reject the moderation task for the given item.
	 * 
	 * @param itemUuid The UUID of the item.
	 * @param itemVersion The version of the item.
	 * @param taskId The UUID of the task to reject.
	 * @param rejectMessage The rejection reason.
	 * @param toStepId The UUID of the task to reject back to.
	 * @param unlock Whether or not to unlock the item after accepting.
	 * @return The UUID of the task that was rejected.
	 * @throws Exception
	 */
	String rejectTask(String itemUuid, int itemVersion, String taskId, String rejectMessage, String toStepId,
		boolean unlock);

	/**
	 * @param schemaUuid The unique ID of the schema
	 * @return XML of format: <div class="block">
	 * 
	 *         <pre>
	 * &lt;tasks&gt;
	 *    Schema XML
	 *    ...
	 * &lt;/tasks&gt;
	 * </pre>
	 * 
	 *         </div>
	 *         <em>See the section on XML Formats in {@link com.tle.web.remoting.soap SOAP Services} for the format of the Schema XML</em>
	 * @throws Exception
	 */
	String getSchema(String schemaUuid);

	/**
	 * @param collectionUuid The unique ID of the collection
	 * @return XML of format: <div class="block">
	 * 
	 *         <pre>
	 * &lt;tasks&gt;
	 *    Collection XML
	 *    ...
	 * &lt;/tasks&gt;
	 * </pre>
	 * 
	 *         </div>
	 *         <em>See the section on XML Formats in {@link com.tle.web.remoting.soap SOAP Services} for the format of the Collection XML</em>
	 * @throws Exception
	 */
	String getCollection(String collectionUuid);

	/**
	 * Retrieve a single comment for an item by ID.
	 * 
	 * @param itemUuid UUID of the item.
	 * @param itemVersion Version of the item.
	 * @param commentUuid UUID of the comment to retrieve.
	 * @return XML of format: <div class="block">
	 * 
	 *         <pre>
	 * &lt;comment&gt;
	 *     &lt;uuid&gt;...         Identifier for comment on this item
	 *     &lt;rating&gt;...       Non-existent for no rating else a value between 1 and 5 inclusive
	 *     &lt;text&gt;...         Non-existent for no comment, else the comment text made by the user
	 *     &lt;owner&gt;...        ID of user writing this thread.  Empty for guest or anonymous comments/ratings.
	 *     &lt;dateCreated&gt;...  Full ISO format including time zone, eg, 2011-02-09T10:53:23+1000
	 * &lt;/comment&gt;
	 * </pre>
	 * 
	 *         </div>
	 */
	String getComment(String itemUuid, int itemVersion, String commentUuid);

	/**
	 * Retrieve several comments for an item.
	 * 
	 * @param itemUuid UUID of the item.
	 * @param itemVersion Version of the item.
	 * @param filter Bit-mask indicating what filters, if any, to apply to the
	 *            results. A value of zero indicates no filtering.
	 *            <ul>
	 *            <li><b>1</b> Must have non-empty comment text</li>
	 *            <li><b>2</b> Must have a rating</li>
	 *            <li><b>4</b> Comment/rating must be from a known user, not
	 *            from a guest session or comment marked as anonymous</li>
	 *            <li><b>8</b> Only the most recent comment/rating per user</li>
	 *            </ul>
	 *            For example, '9' would only return the most recent non-empty
	 *            comment per user.
	 * @param order Integer indicating the order of results returned:
	 *            <ul>
	 *            <li><b>1</b> to order by most recent (reverse chronological)</li>
	 *            <li><b>2</b> to order by earliest (chronological)</li>
	 *            <li><b>3</b> to order by highest rating</li>
	 *            <li><b>4</b> for lowest rating</li>
	 *            </ul>
	 * @param limit Integer indicating the maximum number of comments to return.
	 *            A value of zero or less will return all comments.
	 * @return XML of format: <div class="block">
	 * 
	 *         <pre>
	 * &lt;comments average="3.5"&gt;  Current average rating for the item. -1 no ratings.
	 *     &lt;comment&gt;...          See return XML on getComment()
	 *     &lt;comment&gt;...
	 *     ...
	 * &lt;/comments&gt;
	 * </pre>
	 * 
	 *         </div>
	 */
	String getComments(String itemUuid, int itemVersion, int filter, int order, int limit);

	/**
	 * Add a new comment to an item.
	 * 
	 * @param itemUuid UUID of the item.
	 * @param itemVersion Version of the item.
	 * @param commentText Comment text to add. Blank if only a rating.
	 * @param rating Integer where 0 indicates no rating, other wise between 1
	 *            and 5 inclusive.
	 * @param anonymous true if the identify of the comment author should be
	 *            hidden when comments are being viewed. The author user ID is
	 *            still recorded for auditing purposes.
	 */
	void addComment(String itemUuid, int itemVersion, String commentText, int rating, boolean anonymous);

	/**
	 * Delete a single comment for an item by ID.
	 * 
	 * @param itemUuid UUID of the item.
	 * @param itemVersion Version of the item.
	 * @param commentUuid UUID of the comment to delete.
	 */
	void deleteComment(String itemUuid, int itemVersion, String commentUuid);
}
