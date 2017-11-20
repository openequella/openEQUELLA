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

package com.tle.core.connectors.moodle.service;

import static com.tle.core.connectors.moodle.MoodleConnectorConstants.ARCHIVED_PARAM;
import static com.tle.core.connectors.moodle.MoodleConnectorConstants.COURSE_CODE_PARAM;
import static com.tle.core.connectors.moodle.MoodleConnectorConstants.COURSE_ID_PARAM;
import static com.tle.core.connectors.moodle.MoodleConnectorConstants.KEY_PATH;
import static com.tle.core.connectors.moodle.MoodleConnectorConstants.NAME_NODE;
import static com.tle.core.connectors.moodle.MoodleConnectorConstants.SECTION_ID_PARAM;
import static com.tle.core.connectors.moodle.MoodleConnectorConstants.SINGLE_KEY_PATH;
import static com.tle.core.connectors.moodle.MoodleConnectorConstants.SUCCESS_KEY;
import static com.tle.core.connectors.moodle.MoodleConnectorConstants.USER_PARAM;
import static com.tle.core.connectors.moodle.MoodleConnectorConstants.VALUE_NODE;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.impl.PluginServiceImpl;
import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.dytech.common.io.UnicodeReader;
import com.dytech.edge.common.Constants;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ViewableItemType;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.URLUtils;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.moodle.EntityStrippingWriter;
import com.tle.core.connectors.moodle.MoodleConnectorConstants;
import com.tle.core.connectors.service.AbstractIntegrationConnectorRespository;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.core.guice.Bind;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Request.Method;
import com.tle.core.services.http.Response;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.xml.XmlDocument;
import com.tle.web.integration.Integration.LmsLink;
import com.tle.web.integration.Integration.LmsLinkInfo;
import com.tle.web.selection.SelectedResource;

@NonNullByDefault
@Bind
@Singleton
public class MoodleConnectorService extends AbstractIntegrationConnectorRespository
{
	private static final Logger LOGGER = Logger.getLogger(MoodleConnectorService.class);

	@Inject
	private HttpService httpService;
	@Inject
	private ConfigurationService configService;
	private static String KEY_PFX = AbstractPluginService.getMyPluginId(MoodleConnectorService.class)+".";

	@Override
	public boolean isRequiresAuthentication(Connector connector)
	{
		return false;
	}

	@Nullable
	@Override
	public String getAuthorisationUrl(Connector connector, String forwardUrl, @Nullable String authData)
	{
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String sectionId,
		IItem<?> item, SelectedResource selectedResource) throws LmsUserNotFoundException
	{
		final LmsLinkInfo lmsLinkInfo = getLmsLink(item, selectedResource);
		final LmsLink lmsLink = lmsLinkInfo.getLmsLink();
		final IItem<?> resourcesItem = lmsLinkInfo.getResourceItem();

		final MoodleWebService ws = setupService(connector);
		final Map<String, String> data = functionCall(ws, "equella_add_item_to_course");
		param(data, USER_PARAM, username);
		param(data, COURSE_ID_PARAM, courseId);
		param(data, SECTION_ID_PARAM, sectionId);
		param(data, "itemUuid", resourcesItem.getUuid());
		param(data, "itemVersion", String.valueOf(selectedResource.isLatest() ? 0 : resourcesItem.getVersion()));
		param(data, "url", lmsLink.getUrl());
		param(data, "title", lmsLink.getName());
		param(data, "description", lmsLink.getDescription());
		final IAttachment attachment = lmsLinkInfo.getResourceAttachment();
		param(data, "attachmentUuid", attachment == null ? "" : attachment.getUuid());

		ConnectorCourse course = null;
		ConnectorFolder folder = null;
		final XmlDocument response = ws.call(data);
		for( Node keyNode : response.nodeList(KEY_PATH) )
		{
			final String key = keyNode.getAttributes().getNamedItem(NAME_NODE).getNodeValue();
			final String value = response.nodeValue(VALUE_NODE, keyNode);

			if( key.equals(COURSE_ID_PARAM) )
			{
				course = new ConnectorCourse(value);
			}
			else if( key.equals("coursename") )
			{
				course.setName(value);
			}
			else if( key.equals(SECTION_ID_PARAM) )
			{
				folder = new ConnectorFolder(value, course);
			}
			else if( key.equals("sectionname") )
			{
				folder.setName(value);
				course.addFolder(folder);
			}
		}
		return folder;
	}

	@Override
	protected ViewableItemType getViewableItemType()
	{
		return ViewableItemType.GENERIC;
	}

	@Override
	protected String getIntegrationId()
	{
		return "gen";
	}

	@Override
	protected boolean isRelativeUrls()
	{
		return false;
	}

	@Override
	@SuppressWarnings("null")
	public List<ConnectorCourse> getCourses(Connector connector, String username, boolean editable, boolean archived,
		boolean management) throws LmsUserNotFoundException
	{
		final MoodleWebService ws = setupService(connector);
		final Map<String, String> data = functionCall(ws, "equella_list_courses_for_user");
		param(data, USER_PARAM, username);
		param(data, "modifiable", editable);
		param(data, ARCHIVED_PARAM, archived);
		final List<ConnectorCourse> courses = new ArrayList<ConnectorCourse>();

		ConnectorCourse course = null;
		final XmlDocument response = ws.call(data);
		for( Node keyNode : response.nodeList(KEY_PATH) )
		{
			final String key = keyNode.getAttributes().getNamedItem(NAME_NODE).getNodeValue();
			final String value = response.nodeValue(VALUE_NODE, keyNode);

			if( key.equals(COURSE_ID_PARAM) )
			{
				if( course != null )
				{
					courses.add(course);
				}
				course = new ConnectorCourse(value);
			}
			else if( key.equals(COURSE_CODE_PARAM) )
			{
				course.setCourseCode(value);
			}
			else if( key.equals("coursename") )
			{
				course.setName(value);
			}
			else if( key.equals(ARCHIVED_PARAM) )
			{
				course.setAvailable(value.equals("0"));
			}
		}
		if( course != null )
		{
			String courseCode = getCourseCode(connector, username, course.getId());
			course.setCourseCode(courseCode);
			courses.add(course);
		}
		return courses;
	}

	@Override
	@SuppressWarnings("null")
	public List<ConnectorFolder> getFoldersForCourse(Connector connector, String username, String courseId,
		boolean management) throws LmsUserNotFoundException
	{
		final MoodleWebService ws = setupService(connector);
		final Map<String, String> data = functionCall(ws, "equella_list_sections_for_course");
		param(data, USER_PARAM, username);
		param(data, COURSE_ID_PARAM, courseId);

		final List<ConnectorFolder> folders = new ArrayList<ConnectorFolder>();

		final ConnectorCourse course = new ConnectorCourse(courseId);
		ConnectorFolder folder = null;
		final XmlDocument response = ws.call(data);
		for( Node keyNode : response.nodeList(KEY_PATH) )
		{
			final String key = keyNode.getAttributes().getNamedItem(NAME_NODE).getNodeValue();
			final String value = response.nodeValue(VALUE_NODE, keyNode);

			if( key.equals(SECTION_ID_PARAM) )
			{
				if( folder != null )
				{
					folders.add(folder);
				}
				folder = new ConnectorFolder(value, course);
				folder.setLeaf(true);
			}
			else if( key.equals("sectionname") )
			{
				folder.setName(value);
			}
		}

		if( folder != null )
		{
			folders.add(folder);
		}

		return folders;
	}

	@Override
	public List<ConnectorFolder> getFoldersForFolder(Connector connector, String username, String courseId,
		String folderId, boolean management) throws LmsUserNotFoundException
	{
		return Lists.newArrayList();
	}

	@Override
	public List<ConnectorContent> findUsages(Connector connector, String username, String uuid, int version,
		boolean versionIsLatest, boolean archived, boolean allVersion) throws LmsUserNotFoundException
	{
		MoodleWebService ws = setupService(connector);
		final Map<String, String> data = functionCall(ws, "equella_find_usage_for_item");
		param(data, USER_PARAM, username);
		param(data, "uuid", uuid);
		param(data, "version", version);
		param(data, ARCHIVED_PARAM, archived);
		param(data, "allVersion", allVersion);
		param(data, "isLatest", versionIsLatest);

		XmlDocument response = ws.call(data);
		return parseResponse(response, connector.getServerUrl());
	}

	@Override
	public SearchResults<ConnectorContent> findAllUsages(Connector connector, String username, String query,
		String courseId, String folderId, boolean archived, int offset, int count, ExternalContentSortType sortType,
		boolean reverseSort) throws LmsUserNotFoundException
	{
		MoodleWebService ws = setupService(connector);
		final Map<String, String> data = functionCall(ws, "equella_find_all_usage");
		param(data, USER_PARAM, username);
		param(data, "query", query.replace("*", "%"));
		param(data, COURSE_ID_PARAM, Check.isEmpty(courseId) ? 0 : Integer.valueOf(courseId));
		param(data, SECTION_ID_PARAM, Check.isEmpty(folderId) ? 0 : Integer.valueOf(folderId));
		param(data, ARCHIVED_PARAM, archived);
		param(data, "offset", offset);
		param(data, "count", count);

		final String sortColumn;
		switch( sortType )
		{
			case COURSE:
				sortColumn = "course";
				break;
			case NAME:
				sortColumn = NAME_NODE;
				break;
			case DATE_ADDED:
			default:
				sortColumn = "timecreated";
				break;
		}
		param(data, "sortcolumn", sortColumn);
		param(data, "sortasc", sortType == ExternalContentSortType.DATE_ADDED ? reverseSort : !reverseSort);

		final XmlDocument response = ws.call(data);
		final List<ConnectorContent> content = parseResponse(response, connector.getServerUrl());
		final String available = getValue(response, "/RESPONSE/SINGLE/", "available", null);
		if( available == null )
		{
			// some error has occurred...
			throw new RuntimeException("Error contacting Moodle server");
		}
		return new SimpleSearchResults<ConnectorContent>(content, content.size(), offset, Integer.valueOf(available));
	}

	public String getValue(XmlDocument xml, String key, Node nodeContext)
	{
		return getValue(xml, "", key, nodeContext);
	}

	public String getValue(XmlDocument xml, String precedingXpath, String key, Node nodeContext)
	{
		return xml.nodeValue(precedingXpath + "KEY[@name=\"" + key + "\"]/VALUE", nodeContext);
	}

	@Override
	public int getUnfilteredAllUsagesCount(Connector connector, String username, String query, boolean archived)
		throws LmsUserNotFoundException
	{
		MoodleWebService ws = setupService(connector);
		final Map<String, String> data = functionCall(ws, "equella_unfiltered_usage_count");
		param(data, USER_PARAM, username);
		param(data, "query", query);
		param(data, ARCHIVED_PARAM, archived);

		final XmlDocument response = ws.call(data);

		for( Node keyNode : response.nodeList(SINGLE_KEY_PATH) )
		{
			final String key = keyNode.getAttributes().getNamedItem(NAME_NODE).getNodeValue();
			final String value = response.nodeValue(VALUE_NODE, keyNode);

			if( key.equals("available") )
			{
				return Integer.parseInt(value);
			}
		}

		return 0;
	}

	@SuppressWarnings("null")
	private List<ConnectorContent> parseResponse(XmlDocument response, String moodleServerUrl)
	{
		ArrayList<ConnectorContent> contentList = new ArrayList<ConnectorContent>();
		ConnectorContent content = null;
		String attributeKey = null;
		Node resultsRoot = response.node("/RESPONSE/SINGLE/KEY[@name=\"results\"]/MULTIPLE");
		for( Node singleNode : response.nodeList("SINGLE", resultsRoot) )
		{
			for( Node keyNode : response.nodeList("." + KEY_PATH, singleNode) )
			{
				final String key = keyNode.getAttributes().getNamedItem(NAME_NODE).getNodeValue();
				final String value = response.nodeValue(VALUE_NODE, keyNode);

				if( key.equals("id") )
				{
					content = new ConnectorContent(value);
					contentList.add(content);
					content.setExternalUrl(
						URLUtils.newURL(moodleServerUrl, "mod/equella/view.php?id=" + value).toString());
				}
				if( key.equals("coursename") )
				{
					content.setCourse(value);
				}
				else if( key.equals(COURSE_ID_PARAM) )
				{
					content.setCourseUrl(URLUtils.newURL(moodleServerUrl, "course/view.php?id=" + value).toString());
				}
				else if( key.equals("section") )
				{
					content.setFolder(value);
				}
				// else if( key.equals(SECTION_ID_PARAM) )
				// {
				// No, this is for folders, not sections. In fact, you can't
				// add to folders via the 'push' interface. A bit of an
				// oversight...
				// if( !Check.isEmpty(value) )
				// {
				// int folderId = Integer.parseInt(value);
				// if( folderId != 0 )
				// {
				// content.setFolderUrl(URLUtils.newURL(moodleServerUrl,
				// "mod/folder/view.php?id=" + value).toString());
				// }
				// }
				// }
				else if( key.equals("dateAdded") )
				{
					content.setDateAdded(new Date(Long.parseLong(value)));
				}
				else if( key.equals("dateModified") )
				{
					content.setDateModified(new Date(Long.parseLong(value)));
				}
				else if( key.equals("uuid") )
				{
					content.setUuid(value);
				}
				else if( key.equals("version") )
				{
					if( !Check.isEmpty(value) )
					{
						content.setVersion(Integer.parseInt(value));
					}
				}
				else if( key.equals("moodlename") )
				{
					content.setExternalTitle(value);
				}
				else if( key.equals("moodledescription") )
				{
					content.setExternalDescription(value);
				}
				else if( key.equals("attachment") )
				{
					content.setAttachmentUrl(value);
				}
				else if( key.equals("attachmentUuid") )
				{
					content.setAttachmentUuid(value);
				}
				else if( key.equals("coursecode") )
				{
					content.setCourseCode(value);
				}
				else if( key.equals("instructor") )
				{
					if( !Check.isEmpty(value) )
					{
						content.setAttribute(ConnectorContent.KEY_INSTRUCTOR, getKey("moodle.finduses.instructor"), value);
					}
				}
				else if( key.equals("dateAccessed") )
				{
					if( !Check.isEmpty(value) )
					{
						content.setAttribute(ConnectorContent.KEY_DATE_ACCESSED, getKey("moodle.finduses.dateAccessed"),
							new Date(Long.parseLong(value)));
					}
				}
				else if( key.equals("enrollments") )
				{
					if( !Check.isEmpty(value) )
					{
						content.setAttribute(ConnectorContent.KEY_ENROLLMENTS, getKey("moodle.finduses.enrollments"),
							Integer.valueOf(value));
					}
				}
				else if( key.equals("visible") )
				{
					content.setAvailable(Integer.parseInt(value) == 1 ? true : false);
					content.setAttribute("visible", getKey("moodle.finduses.visible"),
						Integer.parseInt(value) == 1 ? true : false);
				}

				else if( key.equals("key") )
				{
					attributeKey = value;
				}
				else if( key.equals(VALUE_NODE) )
				{
					if( !Check.isEmpty(value) )
					{
						content.setAttribute(attributeKey, getKey("moodle.finduses." + attributeKey), value);
					}
				}
			}
		}
		return contentList;
	}

	@Override
	public String getCourseCode(Connector connector, String username, String courseId) throws LmsUserNotFoundException
	{
		final MoodleWebService ws = setupService(connector);
		final Map<String, String> data = functionCall(ws, "equella_get_course_code");
		param(data, USER_PARAM, username);
		param(data, COURSE_ID_PARAM, courseId);

		XmlDocument response = ws.call(data);

		String courseCode = null;
		for( Node keyNode : response.nodeList(KEY_PATH) )
		{
			final String key = keyNode.getAttributes().getNamedItem(NAME_NODE).getNodeValue();
			final String value = response.nodeValue(VALUE_NODE, keyNode);

			if( key.equals("coursecode") )
			{
				courseCode = value;
			}
		}

		return courseCode;
	}

	private String errorString(String partKey)
	{
		return CurrentLocale.get("com.tle.core.connectors.moodle." + partKey);
	}

	private MoodleWebService setupService(Connector connector)
	{
		return setupService(connector.getServerUrl(),
			connector.getAttribute(MoodleConnectorConstants.FIELD_WEBSERVICE_TOKEN));
	}

	private MoodleWebService setupService(String serverUrl, String webServiceToken)
	{
		return new MoodleWebService(URLUtils.newURL(serverUrl, "webservice/rest/server.php").toString(),
			webServiceToken);
	}

	private Map<String, String> functionCall(MoodleWebService ws, String function)
	{
		final Map<String, String> params = Maps.newHashMap();
		params.put("wsfunction", function);
		params.put("wstoken", ws.getToken());
		return params;
	}

	private static void param(Map<String, String> params, String param, boolean content)
	{
		param(params, param, content ? "1" : "0");
	}

	private static void param(Map<String, String> params, String param, int content)
	{
		param(params, param, Integer.toString(content));
	}

	private static void param(Map<String, String> params, String param, String content)
	{
		params.put(param, content);
	}

	public class MoodleWebService
	{
		private static final String ERROR_USER_NOT_FOUND = "error/UserNotFound/";
		private final String url;
		private final String token;

		protected MoodleWebService(String url, String token)
		{
			this.url = url;
			this.token = token;
		}

		/**
		 * @param params
		 * @return
		 * @throws LmsUserNotFoundException, MoodleException
		 */
		public XmlDocument call(Map<String, String> params) throws LmsUserNotFoundException
		{
			final Request request = new Request(url);
			request.setAccept("application/xml; charset=utf-8");
			request.setMethod(Method.POST);

			final List<NameValue> nvs = Lists.newArrayList();
			for( Entry<String, String> entry : params.entrySet() )
			{
				nvs.add(new NameValue(entry.getKey(), entry.getValue()));
			}
			request.setHtmlForm(nvs);

			try( Response response = httpService.getWebContent(request, configService.getProxyDetails()) )
			{
				final EntityStrippingWriter writer = new EntityStrippingWriter(new StringWriter());
				final InputSource s = new InputSource();
				s.setEncoding(Constants.UTF8);
				s.setCharacterStream(new UnicodeReader(response.getInputStream(), "UTF-8"));

				final XMLReader r = new Parser();
				r.setContentHandler(writer);
				r.parse(s);

				final XmlDocument xml = new XmlDocument(writer.getOutput());

				// Used for testing
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Read from server:");
					LOGGER.debug(xml.toString());
				}

				// check to see if XML contains EXCEPTION as first node!
				final Node exception = xml.node("EXCEPTION");
				if( exception != null )
				{
					String message = xml.nodeValue("MESSAGE", exception);
					if( message.startsWith(ERROR_USER_NOT_FOUND) )
					{
						String username = message.substring(ERROR_USER_NOT_FOUND.length());
						throw new LmsUserNotFoundException(username,
							CurrentLocale.get(getKey("connector.error"), username));
					}
					throw new MoodleException("Error contacting Moodle: " + message);
				}

				return xml;
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}

		public String getUrl()
		{
			return url;
		}

		public String getToken()
		{
			return token;
		}
	}

	private static class MoodleException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public MoodleException(String message)
		{
			super(message);
		}
	}

	private String getKey(String partKey)
	{
		return KEY_PFX + partKey;
	}

	@Override
	public ConnectorTerminology getConnectorTerminology()
	{
		ConnectorTerminology terms = new ConnectorTerminology();
		terms.setShowArchived(getKey("moodle.finduses.showarchived"));
		terms.setShowArchivedLocations(getKey("moodle.finduses.showarchived.courses"));
		terms.setCourseHeading(getKey("moodle.finduses.course"));
		terms.setLocationHeading(getKey("moodle.finduses.location"));
		return terms;
	}

	@Override
	public boolean supportsExport()
	{
		return true;
	}

	@Override
	public boolean supportsEdit()
	{
		return true;
	}

	@Override
	public boolean supportsView()
	{
		return true;
	}

	@Override
	public boolean supportsDelete()
	{
		return true;
	}

	@Override
	public boolean deleteContent(Connector connector, String username, String id) throws LmsUserNotFoundException
	{
		final MoodleWebService ws = setupService(connector);
		final Map<String, String> data = functionCall(ws, "equella_delete_item");
		param(data, USER_PARAM, username);
		param(data, "itemid", id);

		XmlDocument response = ws.call(data);
		for( Node keyNode : response.nodeList(SINGLE_KEY_PATH) )
		{
			final String key = keyNode.getAttributes().getNamedItem(NAME_NODE).getNodeValue();
			final String value = response.nodeValue(VALUE_NODE, keyNode);
			if( key.equals(SUCCESS_KEY) )
			{
				return Integer.parseInt(value) == 1;
			}
		}

		return false;
	}

	@Override
	public boolean editContent(Connector connector, String username, String contentId, String title, String description)
		throws LmsUserNotFoundException
	{
		final MoodleWebService ws = setupService(connector);
		final Map<String, String> data = functionCall(ws, "equella_edit_item");
		param(data, USER_PARAM, username);
		param(data, "itemid", contentId);
		param(data, "title", title);
		param(data, "description", description);

		XmlDocument response = ws.call(data);
		for( Node keyNode : response.nodeList(SINGLE_KEY_PATH) )
		{
			final String key = keyNode.getAttributes().getNamedItem(NAME_NODE).getNodeValue();
			final String value = response.nodeValue(VALUE_NODE, keyNode);
			if( key.equals(SUCCESS_KEY) )
			{
				return Integer.parseInt(value) == 1;
			}
		}

		return false;
	}

	@Override
	public boolean moveContent(Connector connector, String username, String contentId, String courseId,
		String locationId) throws LmsUserNotFoundException
	{
		final MoodleWebService ws = setupService(connector);
		final Map<String, String> data = functionCall(ws, "equella_move_item");
		param(data, USER_PARAM, username);
		param(data, "itemid", contentId);
		param(data, COURSE_ID_PARAM, courseId);
		param(data, "locationid", locationId);

		XmlDocument response = ws.call(data);
		for( Node keyNode : response.nodeList(SINGLE_KEY_PATH) )
		{
			final String key = keyNode.getAttributes().getNamedItem(NAME_NODE).getNodeValue();
			final String value = response.nodeValue(VALUE_NODE, keyNode);
			if( key.equals(SUCCESS_KEY) )
			{
				return Integer.parseInt(value) == 1;
			}
		}

		return false;
	}

	@Override
	public boolean supportsCourses()
	{
		return true;
	}

	public String testConnection(String serverUrl, String webServiceToken, String username)
	{
		final MoodleWebService ws = setupService(serverUrl, webServiceToken);

		if( Check.isEmpty(serverUrl) )
		{
			return errorString("connector.test.error.nourl");
		}
		if( Check.isEmpty(ws.getToken()) )
		{
			return errorString("connector.test.error.notoken");
		}

		// try the URL. Given that this is a test connection method, we aren't
		// fussy about what Exception if any is thrown or the precise semantics
		// of how we catch & handle it
		try
		{
			final Request request = new Request(ws.getUrl());
			request.setMethod(Method.GET);
			httpService.getWebContent(request, configService.getProxyDetails());
		}
		catch( Exception e ) // NOSONAR
		{
			return errorString("connector.test.error.unreachableurl");
		}

		// call the webservice test function
		final Map<String, String> data = functionCall(ws, "equella_test_connection");
		param(data, "param", username);

		try
		{
			XmlDocument response = ws.call(data);
			if( response.nodeList(SINGLE_KEY_PATH).size() > 0 )
			{
				for( Node keyNode : response.nodeList(SINGLE_KEY_PATH) )
				{
					final String key = keyNode.getAttributes().getNamedItem(NAME_NODE).getNodeValue();
					final String value = response.nodeValue(VALUE_NODE, keyNode);

					if( LOGGER.isDebugEnabled() )
					{
						LOGGER.debug("key: " + key + "   value: " + value);
					}

					if( key == null || !key.equals(SUCCESS_KEY) || value == null || !value.equals(username) )
					{
						return errorString("connector.test.error.invalidresponse");
					}
				}
			}
			else
			{
				return errorString("connector.test.error.emptyresponse");
			}
		}
		catch( Exception m )
		{
			return m.getMessage();
		}

		return null;
	}

	@Override
	public boolean supportsFindUses()
	{
		return true;
	}

	@Override
	public boolean supportsReverseSort()
	{
		return true;
	}
}
