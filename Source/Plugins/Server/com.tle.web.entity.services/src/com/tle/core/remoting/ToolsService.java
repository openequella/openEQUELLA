package com.tle.core.remoting;

/**
 * Additional methods to those included in
 * {@link com.tle.core.remoting.SoapInterfaceV1} and
 * {@link com.tle.core.remoting.SoapInterfaceV2}
 */
public interface ToolsService
{
	/**
	 * Get the "Unique ID" of the currently logged in user. This is usually a
	 * login username.
	 * 
	 * @param ssUuid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @return The unique ID of the currently logged in user
	 */
	String getUserId(String ssUuid);

	/**
	 * This method duplicates the functionality of SoapInterfaceV1's login
	 * method.<br>
	 * <b>You only need to call either one of these methods, and only once per
	 * session.</b><br>
	 * Note that the response sent back by this method will include a new
	 * cookie. Your client MUST have cookies enabled. This method will throw an
	 * exception if Authentication fails.
	 * 
	 * @see SoapInterfaceV1.login(String, String)
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
	 * @param ssUuid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param lastUpdate
	 * @return A list of additions and removals of item keys eg
	 *         "+UUID/version,-UUID/version"
	 */
	String[] getCacheList(String ssUuid, String lastUpdate);

	/**
	 * @param ssUuid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @return
	 */
	String getCacheSchedule(String ssUuid);

	/**
	 * Creates a new version of an existing item.
	 * 
	 * @param ssUuid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param uuid The UUID of the item to create a new version of
	 * @param version The version of the item to create a new version of
	 * @param copyAttachments Include the attachments of the original item with
	 *            the new version
	 * @return The XML of the new item
	 */
	String newVersion(String ssUuid, String uuid, int version, boolean copyAttachments);

	/**
	 * Archives an item. Will not delete the item if it is already archived.
	 * 
	 * @param ssUuid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 * @param uuid The UUID of the item to archive
	 * @param version The version of the item to archive
	 * @param itemdef Not used
	 */
	void archive(String ssUuid, String uuid, int version, String itemdef);
}
