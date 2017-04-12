package com.tle.blackboard.webservice;

import blackboard.platform.ws.anns.AuthenticatedMethod;

// God damn comment formatting.
/**
 * Please use ContextWS.emulateUser before invoking any method.
 * <p>
 * Error codes:
 * <table summary="Error codes">
 * <tr>
 * <td>EQ001</td>
 * <td>Error getting persistence manager</td>
 * </tr>
 * <tr>
 * <td>EQ002</td>
 * <td>Error generating ID</td>
 * </tr>
 * <tr>
 * <td>EQ003</td>
 * <td>Error saving item</td>
 * </tr>
 * <tr>
 * <td>EQ004</td>
 * <td>Invalid item</td>
 * </tr>
 * <tr>
 * <td>EQ005</td>
 * <td>Error listing courses for user</td>
 * </tr>
 * <tr>
 * <td>EQ006</td>
 * <td>Error listing folders for course</td>
 * </tr>
 * <tr>
 * <td>EQ007</td>
 * <td>Error listing folders for folder</td>
 * </tr>
 * <tr>
 * <td>EQ010</td>
 * <td>Error synchronising content</td>
 * </tr>
 * <tr>
 * <td>EQ011</td>
 * <td>Error finding usage</td>
 * </tr>
 * <tr>
 * <td>EQ012</td>
 * <td>Error getting course code</td>
 * </tr>
 * <tr>
 * <td>EQ013</td>
 * <td>Error deleting content</td>
 * </tr>
 * <tr>
 * <td>EQ014</td>
 * <td>Error editing content</td>
 * </tr>
 * <tr>
 * <td>EQ015</td>
 * <td>Error moving content</td>
 * </tr>
 * <tr>
 * <td>EQ100</td>
 * <td>Permission denied</td>
 * </tr>
 * </table>
 */
// @NonNullByDefault
public interface EquellaWebservice
{
	/**
	 * Returns the current version of this web service on the server
	 * 
	 * @return The version of the webservice. This will only be incremented once
	 *         the webservice is changed and there exists a released version of
	 *         the old webservice in the wild.
	 * @since 1
	 */
	int getServerVersion();

	/**
	 * Tests that the EQUELLA webservice is reachable
	 * 
	 * @param param A parameter to be echoed back
	 * @return The value of param
	 * @since 1
	 */
	String testConnection(String param);

	/**
	 * @param username unused. Please use ContextWS.emulateUser before invoking
	 *            this method.
	 * @return A shallow load of AVAILABLE courses that user is enrolled in
	 * @since 2
	 */
	Course[] listCoursesForUser(String username, boolean archived, boolean modifiableOnly);

	/**
	 * @param courseId
	 * @return A shallow load of folders
	 * @since 1
	 */
	Folder[] listFoldersForCourse(String courseId);

	/**
	 * @param folderId
	 * @return A shallow load of folders
	 * @since 1
	 */
	Folder[] listFoldersForFolder(String folderId);

	/**
	 * @param username unused. Please use ContextWS.emulateUser before invoking
	 *            this method.
	 * @param courseid
	 * @param folderId
	 * @param itemUuid
	 * @param itemVersion
	 * @param url Appears to be of the form ?attachment.uuid=blah
	 * @param title
	 * @param description
	 * @param xml
	 * @param serverUrl
	 * @param attachmentUuid UUID of the attachment. Specify null for the item
	 *            summary
	 * @return The folder it was successfully added to. Or null if not.
	 * @since 1
	 */
	@AuthenticatedMethod(entitlements = {"course.content.CREATE"}, checkEntitlement = true)
	AddItemResult addItemToCourse(String username, String courseid, String folderId, String itemUuid, int itemVersion,
		String url, String title, String description, String xml, String serverUrl, String attachmentUuid);

	/**
	 * @param serverUrl The institution URL. Should be the same as configured in
	 *            the Building Block
	 * @param itemUuid The uuid of the item to get usage for.
	 * @param itemVersion The version of the item to get usage for. Unused if
	 *            allVersions specified
	 * @param available If true, will only check courses that are available
	 * @param allVersions Find all versions of said item
	 * @return An array of folders with populated course details
	 * @since 1
	 */
	SearchResult findUsages(String serverUrl, String itemUuid, int itemVersion, boolean versionIsLatest,
		boolean available, boolean allVersions);

	/**
	 * @param serverUrl The institution URL. Should be the same as configured in
	 *            the Building Block
	 * @param query A freetext query to match external content name. ie. the
	 *            name of the resource in Blackboard
	 * @param courseId restrict to this course
	 * @param folderId restrict to this folder
	 * @param available If true, will only check courses that are available
	 * @param offset Search paging
	 * @param count Search paging
	 * @param sortColumn "name" or "dateAdded"
	 * @param sortReverse
	 * @return An array of folders with populated course details
	 * @since 2
	 */
	SearchResult findAllUsages(String serverUrl, String query, String courseId, String folderId, boolean available,
		int offset, int count, String sortColumn, boolean sortReverse);

	/**
	 * Scans for EQUELLA content and updates the EQUELLA DB table for quick
	 * access
	 * 
	 * @param institutionUrl
	 * @param available Only check available courses and available registered
	 *            content
	 * @return Always true
	 * @since 1
	 */
	boolean synchroniseEquellaContentTables(String institutionUrl, boolean available);

	/**
	 * Get the course code e.g. EQ101 for the supplied Blackboard internal
	 * course ID
	 * 
	 * @param courseId
	 * @return null if the course doesn't have a code (may not even be allowed?)
	 */
	String getCourseCode(String courseId);

	/**
	 * Removes EQUELLA content (well, technically any content) from Blackboard
	 * 
	 * @param contentId
	 * @return true
	 * @since 3
	 */
	boolean deleteContent(String contentId);

	/**
	 * Updates EQUELLA content with a new title and description
	 * 
	 * @param contentId
	 * @param title
	 * @param description
	 * @param institutionUrl
	 * @return true
	 * @since 3
	 */
	boolean editContent(String contentId, String title, String description, String institutionUrl);

	/**
	 * Moves EQUELLA content from one location to another (course and folder)
	 * 
	 * @param contentId
	 * @param courseId
	 * @param folderId
	 * @return true
	 * @since 3
	 */
	boolean moveContent(String contentId, String courseId, String folderId);

	/**
	 * Do not use. Required to prevent two Course elements appearing in WSDL.
	 * 
	 * @return A blank Base
	 */
	Base aBaseReturningMethod();
}
