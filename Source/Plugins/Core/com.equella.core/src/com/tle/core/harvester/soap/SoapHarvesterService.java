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

package com.tle.core.harvester.soap;

/**
 * @author will
 */
public interface SoapHarvesterService
{
	/**
	 * Login with the given username and password.
	 * <p>
	 * Note that the response sent back by this method will include a new
	 * cookie. <b>Your client MUST have cookies enabled</b>.
	 * 
	 * @param username The username
	 * @param password The password
	 * @return The user XML. See the section on XML Formats in
	 *         {@link com.tle.web.remoting.soap SOAP Services}
	 * @throws An exception is thrown if login fails for any reason (e.g.
	 *             invalid credentials)
	 */
	String login(String username, String password) throws Exception;

	/**
	 * Login with the given token. The token format is described in the
	 * "LMS Integration Specification" document.
	 * <p>
	 * Note that the response sent back by this method will include a new
	 * cookie. <b>Your client MUST have cookies enabled</b>.
	 * 
	 * @param token Token string
	 * @return The user XML. See the section on XML Formats in
	 *         {@link com.tle.web.remoting.soap SOAP Services}
	 * @throws An exception is thrown if login fails for any reason (e.g.
	 *             invalid credentials)
	 */
	String loginWithToken(String token) throws Exception;

	/**
	 * Logout the current user.
	 * 
	 * @param ssid The current session id (<strong>Important</strong>: please
	 *            see documentation on
	 *            {@link com.tle.core.remoting.SoapInterfaceV1 sessions})
	 */
	void logout();

	/**
	 * Searches a collection for all the items modified since the supplied date.
	 * 
	 * @param collectionUuids An array of collections uuids to search
	 * @param onlyLive Search only live items
	 * @param since The date to search from in the format of
	 *            "yyyy-MM-dd'T'HH:mm:ssZ"
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
	String searchItemsSince(String[] collectionUuids, boolean onlyLive, String since);

	/**
	 * Returns a list of all collections that the currently logged in user can
	 * search. <br>
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
	 */
	String getSearchableCollections() throws Exception;

	/**
	 * Returns a list of all dynamic collections that the currently logged in
	 * user can search. <br>
	 * <div class="block">
	 * 
	 * <pre>
	 * &lt;xml&gt;
	 *   &lt;dyncol&gt;
	 *     &lt;id&gt;&lt;/id&gt;
	 *     &lt;uuid&gt;&lt;/uuid&gt;
	 *     &lt;name&gt;&lt;/name&gt;
	 *     &lt;system&gt;&lt;/system&gt;
	 *     &lt;virtval&gt;&lt;/virtval&gt;
	 *   &lt;/dyncol&gt;
	 * &lt;/xml&gt;
	 * </pre>
	 * 
	 * </div>
	 */
	String getDynamicCollections() throws Exception;

	/**
	 * Searches a dynamic collection for all the items modified since the
	 * supplied date.
	 * 
	 * @param dynaCollection The dynamic collection id
	 * @param virtualisationValue The virtualisation value if there is one
	 * @param since The date to search from in the format of
	 *            "yyyy-MM-dd'T'HH:mm:ssZ"
	 * @param onlyLive Search only live items
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
	String searchDynamicCollection(String dynaCollection, String virtualisationValue, String since, boolean liveOnly);

	/**
	 * Gets the item xml for the supplied item.<br>
	 * If the item has a DRM acceptance, the logged in use must have accepted it
	 * or the "Skip DRM for harvesters" setting must be set.
	 * 
	 * @param itemUuid The item uuid
	 * @param version The item version
	 * @return The xml for the item
	 */
	String getItemXml(String itemUuid, int version) throws Exception;

	/**
	 * Zips up an item for downloading later by the returned URL. If the item
	 * has a DRM acceptance, the logged in use must have accepted it or the
	 * "Skip DRM for harvesters" setting must be set.
	 * 
	 * @param itemUuid The item id
	 * @param version The item version
	 * @return A URL to a .tar.gz download over standard HTTP.
	 */
	String prepareDownload(String itemUuid, int version) throws Exception;
}
