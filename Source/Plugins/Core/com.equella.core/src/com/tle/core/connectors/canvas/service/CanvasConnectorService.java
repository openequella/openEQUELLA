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

package com.tle.core.connectors.canvas.service;

import static com.tle.core.connectors.canvas.CanvasConnectorConstants.COURSE_STATE_AVAILABLE;
import static com.tle.core.connectors.canvas.CanvasConnectorConstants.COURSE_STATE_UNPUBLISHED;
import static com.tle.core.connectors.canvas.CanvasConnectorConstants.FIELD_ACCESS_TOKEN;
import static com.tle.core.connectors.canvas.CanvasConnectorConstants.MODULE_ITEM_CONTENT_ID;
import static com.tle.core.connectors.canvas.CanvasConnectorConstants.MODULE_ITEM_EXTERNAL_TOOL;
import static com.tle.core.connectors.canvas.CanvasConnectorConstants.MODULE_ITEM_EXTERNAL_URL;
import static com.tle.core.connectors.canvas.CanvasConnectorConstants.MODULE_ITEM_NEW_TAB;
import static com.tle.core.connectors.canvas.CanvasConnectorConstants.MODULE_ITEM_TITLE;
import static com.tle.core.connectors.canvas.CanvasConnectorConstants.MODULE_ITEM_TYPE;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.plugins.AbstractPluginService;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ViewableItemType;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.PathUtils;
import com.tle.common.URLUtils;
import com.tle.common.Utils;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.core.connectors.canvas.CanvasConnectorConstants;
import com.tle.core.connectors.canvas.beans.CanvasAccountBean;
import com.tle.core.connectors.canvas.beans.CanvasCourseBean;
import com.tle.core.connectors.canvas.beans.CanvasErrorBean;
import com.tle.core.connectors.canvas.beans.CanvasErrorBean.CanvasErrorMessageBean;
import com.tle.core.connectors.canvas.beans.CanvasExternalToolBean;
import com.tle.core.connectors.canvas.beans.CanvasModuleBean;
import com.tle.core.connectors.canvas.beans.CanvasModuleItemBean;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.AbstractIntegrationConnectorRespository;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Request.Method;
import com.tle.core.services.http.Response;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.selection.SelectedResource;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class CanvasConnectorService extends AbstractIntegrationConnectorRespository
{
	private static final Logger LOGGER = Logger.getLogger(CanvasConnectorService.class);

	private static String KEY_PFX = AbstractPluginService.getMyPluginId(CanvasConnectorService.class)+".";

	private static final String REQUEST_HEADER_AUTHORIZATION = "Authorization";

	private static final String RESPONSE_HEADER_LINK = "Link";

	private static final String CONTENT_ID_TOKENISER = ":";

	private static final String EXTERNAL_TOOLS = "external_tools";
	private static final String MODULE_ITEMS = "items";
	private static final String MODULES = "modules";
	private static final String COURSES = "courses";
	private static final String ACCOUNTS = "accounts";
	private static final String API_ROOT = "api/v1";

	private final CourseTransformer courseTransformer = new CourseTransformer();

	@Inject
	private HttpService httpService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private InstitutionService institutionService;

	private static final ObjectMapper jsonMapper = new ObjectMapper();

	static
	{
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public boolean isRequiresAuthentication(Connector connector)
	{
		//FIXME: actually, true
		return false;
	}

	@Nullable
	@Override
	public String getAuthorisationUrl(Connector connector, String forwardUrl, @Nullable String authData)
	{
		//FIXME: not actuall null
		return null;
	}

	@Nullable
	@Override
	public String getCourseCode(Connector connector, String username, String courseId) throws LmsUserNotFoundException
	{
		final CanvasCourseBean course = getCanvasCourse(connector, username, courseId);
		return course.getCode();
	}

	@Override
	public List<ConnectorCourse> getCourses(Connector connector, String username, boolean editableOnly,
		boolean archived, boolean management) throws LmsUserNotFoundException
	{
		return new ArrayList<>(
			Lists.transform(getCanvasCourses(connector, username, editableOnly, archived), courseTransformer));
	}

	/**
	 * @return Canvas Modules
	 */
	@Override
	public List<ConnectorFolder> getFoldersForCourse(Connector connector, String username, String courseId,
		boolean management) throws LmsUserNotFoundException
	{
		final ConnectorCourse course = new ConnectorCourse(courseId);
		return new ArrayList<>(
			Lists.transform(getCanvasModules(connector, username, courseId), new ModuleTransformer(course)));
	}

	@Override
	public List<ConnectorFolder> getFoldersForFolder(Connector connector, String username, String courseId,
		String folderId, boolean management) throws LmsUserNotFoundException
	{
		return new ArrayList<>();
	}

	@Override
	public List<ConnectorContent> findUsages(Connector connector, String username, String uuid, int version,
		boolean versionIsLatest, boolean archived, boolean allVersion) throws LmsUserNotFoundException
	{
		// Not currently supported
		return new ArrayList<>();
	}

	@Override
	public int getUnfilteredAllUsagesCount(Connector connector, String username, String query, boolean archived)
		throws LmsUserNotFoundException
	{
		// Not currently supported
		return -1;
	}

	@Override
	public boolean deleteContent(Connector connector, String username, String contentId) throws LmsUserNotFoundException
	{
		final CanvasModuleItemKey moduleKey = parseContentId(contentId);
		final Request request = new Request(apiPath(connector, COURSES, moduleKey.getCourseId(), MODULES,
			moduleKey.getModuleId(), MODULE_ITEMS, moduleKey.getContentId()));
		request.setMethod(Method.DELETE);
		try( final Response response = getCanvasResponse(request, connector, username) )
		{
			return true;
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
	}

	@Override
	public boolean editContent(Connector connector, String username, String contentId, String title, String description)
		throws LmsUserNotFoundException
	{
		final CanvasModuleItemKey moduleKey = parseContentId(contentId);
		final Request request = new Request(apiPath(connector, COURSES, moduleKey.getCourseId(), MODULES,
			moduleKey.getModuleId(), MODULE_ITEMS, moduleKey.getContentId()));
		request.setMethod(Method.PUT);
		final List<NameValue> params = new ArrayList<>();
		if( !title.isEmpty() )
		{
			params.add(new NameValue(MODULE_ITEM_TITLE, title));
		}
		// FYI, there is no description for Canvas module items.
		request.setHtmlForm(params);

		try( final Response response = getCanvasResponse(request, connector, username) )
		{
			return true;
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
	}

	@Override
	public boolean moveContent(Connector connector, String username, String contentId, String courseId, String folderId)
		throws LmsUserNotFoundException
	{
		final CanvasModuleItemKey moduleKey = parseContentId(contentId);
		final Request request = new Request(apiPath(connector, COURSES, moduleKey.getCourseId(), MODULES,
			moduleKey.getModuleId(), MODULE_ITEMS, moduleKey.getContentId()));

		// You can only move content to a different module within the same
		// course
		if( moduleKey.getCourseId().equals(courseId) )
		{
			request.setMethod(Method.PUT);
			final List<NameValue> params = new ArrayList<>();
			params.add(new NameValue("module_item[module_id]", folderId));
			request.setHtmlForm(params);

			try( final Response response = getCanvasResponse(request, connector, username) )
			{
				return true;
			}
			catch( IOException io )
			{
				throw Throwables.propagate(io);
			}
		}
		else
		{
			// For a diff course, this is going to have to be a delete and
			// create
			final CanvasModuleItemBean moduleItem = getCanvasModuleItem(connector, username, moduleKey.getCourseId(),
				moduleKey.getModuleId(), moduleKey.getContentId());
			addCanvasModuleItem(connector, username, courseId, folderId, moduleItem.getTitle(),
				moduleItem.getEquellaUrl());
			deleteContent(connector, username, contentId);
			return true;
		}
	}

	private String getKey(String partKey)
	{
		return KEY_PFX + partKey;
	}

	@Override
	public ConnectorTerminology getConnectorTerminology()
	{
		final ConnectorTerminology terms = new ConnectorTerminology();
		terms.setShowArchived(getKey("canvas.finduses.showarchived"));
		terms.setShowArchivedLocations(getKey("canvas.finduses.showarchived.courses"));
		terms.setCourseHeading(getKey("canvas.finduses.course"));
		terms.setLocationHeading(getKey("canvas.finduses.location"));
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
	public boolean supportsCourses()
	{
		return true;
	}

	@Override
	public boolean supportsReverseSort()
	{
		return true;
	}

	@Override
	protected String getIntegrationId()
	{
		return "gen";
	}

	@Override
	protected boolean isRelativeUrls()
	{
		// We require a full URL for Canvas content
		return false;
	}

	/**
	 * @param folderId This is the Canvas Module ID
	 */
	@Override
	public ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String folderId,
		IItem<?> item, SelectedResource selectedResource) throws LmsUserNotFoundException
	{
		final String title = selectedResource.getTitle();
		final String url = getLmsLink(item, selectedResource).getLmsLink().getUrl();
		addCanvasModuleItem(connector, username, courseId, folderId, title, url);
		return getSingleModule(connector, username, courseId, folderId);
	}

	private void addCanvasModuleItem(Connector connector, String username, String courseId, String folderId,
		String title, String url)
	{
		final CanvasExternalToolBean externalTool = findExternalToolForInst(connector, courseId);

		ArrayList<NameValue> formParams = Lists.newArrayList();
		formParams.add(new NameValue(MODULE_ITEM_TITLE, title));
		formParams.add(new NameValue(MODULE_ITEM_TYPE, MODULE_ITEM_EXTERNAL_TOOL));
		formParams.add(new NameValue(MODULE_ITEM_CONTENT_ID, externalTool.getId()));
		formParams.add(new NameValue(MODULE_ITEM_EXTERNAL_URL, url));
		formParams.add(new NameValue(MODULE_ITEM_NEW_TAB, "true"));

		Request postRequest = new Request(apiPath(connector, COURSES, courseId, MODULES, folderId, MODULE_ITEMS));
		postRequest.addHeader(REQUEST_HEADER_AUTHORIZATION, "Bearer " + connector.getAttribute(FIELD_ACCESS_TOKEN));
		postRequest.setHtmlForm(formParams);
		postRequest.setMethod(Method.POST);

		try( Response response = httpService.getWebContent(postRequest, configService.getProxyDetails()) )
		{
			if( response.isOk() )
			{
				return;
			}
			throw new RuntimeException(response.getMessage());
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private ConnectorFolder getSingleModule(Connector connector, String username, String courseId, String moduleId)
	{
		try
		{
			final ConnectorCourse course = courseTransformer.apply(getCanvasCourse(connector, username, courseId));
			final ConnectorFolder folder = new ModuleTransformer(course)
				.apply(getCanvasModule(connector, username, courseId, moduleId));
			return folder;
		}
		catch( Exception e )
		{
			// just return the barebones folder
		}
		return new ConnectorFolder(moduleId, new ConnectorCourse(courseId));
	}

	private CanvasExternalToolBean findExternalToolForInst(Connector connector, String courseId)
	{
		Request request = new Request(apiPath(connector, COURSES, courseId, EXTERNAL_TOOLS));

		while( request != null )
		{
			// Note: null username param, needs to be done as admin
			try( final Response response = getCanvasResponse(request, connector, null) )
			{
				final List<CanvasExternalToolBean> tools = jsonMapper.readValue(response.getInputStream(),
					new TypeReference<List<CanvasExternalToolBean>>()
					{
						// nada
					});

				for( CanvasExternalToolBean tool : tools )
				{
					if( institutionService.isInstitutionUrl(tool.getUrl()) )
					{
						return tool;
					}
				}
				request = getNextRequest(connector, null, response.getHeader(RESPONSE_HEADER_LINK));
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
		// couldn't find a tool on the course, check account
		final CanvasCourseBean course = getCanvasCourse(connector, null, courseId);

		// FIXME there's supposed to be root_account prop in the course JSON.
		// Currently doesn't work (11/12/14). Can remove CanvasAccountBean and
		// this rest call after fixed
		final CanvasAccountBean account = getCanvasAccount(connector, null, course.getAccountId());
		String rootAccountId = Check.isEmpty(account.getRootAccount()) ? account.getId() : account.getRootAccount();
		request = new Request(apiPath(connector, ACCOUNTS, rootAccountId, EXTERNAL_TOOLS));
		while( request != null )
		{
			try( final Response response = getCanvasResponse(request, connector, null) )
			{
				final List<CanvasExternalToolBean> tools = jsonMapper.readValue(response.getInputStream(),
					new TypeReference<List<CanvasExternalToolBean>>()
					{
						// nada
					});
				for( CanvasExternalToolBean tool : tools )
				{
					if( institutionService.isInstitutionUrl(tool.getUrl()) )
					{
						return tool;
					}
				}
				request = getNextRequest(connector, null, response.getHeader(RESPONSE_HEADER_LINK));
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}

		throw new RuntimeException(CurrentLocale.get(getKey("error.notool")));
	}

	@Override
	public SearchResults<ConnectorContent> findAllUsages(Connector connector, String username, @Nullable String query,
		@Nullable String courseId, @Nullable String folderId, boolean archived, int offset, int count,
		ExternalContentSortType sortType, boolean reverseSort) throws LmsUserNotFoundException
	{
		final List<ConnectorContent> results = new ArrayList<>();
		int ctr = 0;

		final List<CanvasCourseBean> courses = (courseId != null
			? Collections.singletonList(getCanvasCourse(connector, username, courseId))
			: getCanvasCourses(connector, username, false, archived));
		for( CanvasCourseBean course : courses )
		{
			// find the modules
			final List<CanvasModuleBean> modules = (folderId != null
				? Collections.singletonList(getCanvasModule(connector, username, course.getId(), folderId))
				: getCanvasModules(connector, username, course.getId()));
			for( CanvasModuleBean module : modules )
			{
				final Request moduleItemRequest = new Request(
					apiPath(connector, COURSES, course.getId(), MODULES, module.getId(), MODULE_ITEMS));
				if( !Strings.isNullOrEmpty(query) )
				{
					moduleItemRequest.addParameter("search_term", query);
				}

				Request nextRequest = moduleItemRequest;

				while( nextRequest != null )
				{
					try( final Response response = getCanvasResponse(nextRequest, connector, username) )
					{
						final List<CanvasModuleItemBean> items = jsonMapper.readValue(response.getInputStream(),
							new TypeReference<List<CanvasModuleItemBean>>()
							{
								// nada
							});

						final ModuleItemTransformer itemTransformer = new ModuleItemTransformer(connector, course,
							module);

						for( CanvasModuleItemBean item : items )
						{
							ConnectorContent content = itemTransformer.apply(item);
							if( content != null && (ctr >= offset && ctr < offset + count) )
							{
								results.add(content);
								ctr++;
							}
						}

						nextRequest = getNextRequest(connector, username, response.getHeader(RESPONSE_HEADER_LINK));
					}
					catch( Exception e )
					{
						throw Throwables.propagate(e);
					}
				}
			}
		}
		return new SimpleSearchResults<>(results, results.size(), offset, ctr);
	}

	@Override
	protected ViewableItemType getViewableItemType()
	{
		return ViewableItemType.GENERIC;
	}

	public String testAccessToken(String serverUrl, String accessToken)
	{
		// simple rest call and check response code
		try( final Response response = getCanvasResponse(
			new Request(PathUtils.urlPath(serverUrl, API_ROOT, "/users/self/activity_stream")), accessToken, null) )
		{
			if( response.isOk() )
			{
				return "ok";
			}
			return "fail";
		}
		catch( CanvasAuthException ca )
		{
			return "unauthorized";
		}
		catch( Exception e )
		{
			return "fail";
		}
	}

	private String apiPath(Connector connector, String... bits)
	{
		final List<String> encodedBits = new ArrayList<>();
		encodedBits.add(connector.getServerUrl());
		encodedBits.add(API_ROOT);
		for( String bit : bits )
		{
			encodedBits.add(httpService.urlPathEncode(bit));
		}
		return PathUtils.urlPath(encodedBits.toArray(new String[bits.length]));
	}

	private CanvasModuleItemKey parseContentId(String contentId)
	{
		String[] composite = contentId.split(CONTENT_ID_TOKENISER);
		return new CanvasModuleItemKey(composite[0], composite[1], composite[2]);
	}

	private String buildContentId(String courseId, String moduleId, String itemId)
	{
		return Utils.join(new String[]{courseId, moduleId, itemId}, CONTENT_ID_TOKENISER);
	}

	private List<CanvasCourseBean> getCanvasCourses(Connector connector, String username, boolean editableOnly,
		boolean archived)
	{
		final List<CanvasCourseBean> canvasCourses = new ArrayList<>();
		final Request courseRequest = new Request(apiPath(connector, COURSES));
		if( !archived )
		{
			courseRequest.addParameter("state", COURSE_STATE_AVAILABLE);
		}
		Request nextRequest = courseRequest;
		while( nextRequest != null )
		{
			try( Response response = getCanvasResponse(nextRequest, connector, username) )
			{
				final List<CanvasCourseBean> courses = jsonMapper.readValue(response.getInputStream(),
					new TypeReference<List<CanvasCourseBean>>()
					{
						// nada
					});
				for( CanvasCourseBean canvasCourse : courses )
				{
					String state = canvasCourse.getState();
					if( state.equals(COURSE_STATE_AVAILABLE) || state.equals(COURSE_STATE_UNPUBLISHED) )
					{
						canvasCourses.add(canvasCourse);
					}
				}

				nextRequest = getNextRequest(connector, username, response.getHeader(RESPONSE_HEADER_LINK));
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}

		// need to check the accounts of the user
		List<CanvasAccountBean> userAccounts = listUsersAccounts(connector, username);
		for( CanvasAccountBean account : userAccounts )
		{
			final Request accountCourseRequest = new Request(apiPath(connector, ACCOUNTS, account.getId(), COURSES));
			nextRequest = accountCourseRequest;
			while( nextRequest != null )
			{
				try( Response response = getCanvasResponse(nextRequest, connector, username) )
				{
					final List<CanvasCourseBean> courses = jsonMapper.readValue(response.getInputStream(),
						new TypeReference<List<CanvasCourseBean>>()
						{
							// nada
						});
					for( CanvasCourseBean canvasCourse : courses )
					{
						String state = canvasCourse.getState();
						if( !canvasCourses.contains(canvasCourse) && (state.equals(COURSE_STATE_AVAILABLE)
							|| (archived && state.equals(COURSE_STATE_UNPUBLISHED))) )
						{
							canvasCourses.add(canvasCourse);
						}
					}

					nextRequest = getNextRequest(connector, username, response.getHeader(RESPONSE_HEADER_LINK));
				}
				catch( Exception e )
				{
					throw Throwables.propagate(e);
				}
			}
		}
		return canvasCourses;
	}

	private CanvasCourseBean getCanvasCourse(Connector connector, String username, String courseId)
	{
		try
		{
			final String courseResponse = getCanvasJSONResponse(new Request(apiPath(connector, COURSES, courseId)),
				connector, username);
			final CanvasCourseBean canvasCourse = jsonMapper.readValue(courseResponse, CanvasCourseBean.class);
			return canvasCourse;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private CanvasAccountBean getCanvasAccount(Connector connector, String username, String accountId)
	{
		try
		{
			final String accountResponse = getCanvasJSONResponse(new Request(apiPath(connector, ACCOUNTS, accountId)),
				connector, username);
			final CanvasAccountBean canvasAccount = jsonMapper.readValue(accountResponse, CanvasAccountBean.class);
			return canvasAccount;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private List<CanvasAccountBean> listUsersAccounts(Connector connector, String username)
	{
		try
		{
			final String accountsResponse = getCanvasJSONResponse(new Request(apiPath(connector, ACCOUNTS)), connector,
				username);
			// won't do nextRequest stuff. Shouldn't be more than 100 accounts
			return jsonMapper.readValue(accountsResponse, new TypeReference<List<CanvasAccountBean>>()
			{
				// nada
			});

		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private List<CanvasModuleBean> getCanvasModules(Connector connector, String username, String courseId)
	{
		final List<CanvasModuleBean> canvasModules = new ArrayList<>();
		final Request moduleRequest = new Request(apiPath(connector, COURSES, courseId, MODULES));
		Request nextRequest = moduleRequest;
		while( nextRequest != null )
		{
			try( Response response = getCanvasResponse(nextRequest, connector, username) )
			{
				final List<CanvasModuleBean> modules = jsonMapper.readValue(response.getInputStream(),
					new TypeReference<List<CanvasModuleBean>>()
					{
						// nada
					});
				canvasModules.addAll(modules);

				nextRequest = getNextRequest(connector, username, response.getHeader(RESPONSE_HEADER_LINK));
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
		return canvasModules;
	}

	private CanvasModuleBean getCanvasModule(Connector connector, String username, String courseId, String moduleId)
	{
		try
		{
			final String moduleResponse = getCanvasJSONResponse(
				new Request(apiPath(connector, COURSES, courseId, MODULES, moduleId)), connector, username);
			final CanvasModuleBean canvasModule = jsonMapper.readValue(moduleResponse, CanvasModuleBean.class);
			return canvasModule;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private CanvasModuleItemBean getCanvasModuleItem(Connector connector, String username, String courseId,
		String moduleId, String moduleItemId)
	{
		try
		{
			final String moduleResponse = getCanvasJSONResponse(
				new Request(apiPath(connector, COURSES, courseId, MODULES, moduleId, MODULE_ITEMS, moduleItemId)),
				connector, username);
			final CanvasModuleItemBean canvasModule = jsonMapper.readValue(moduleResponse, CanvasModuleItemBean.class);
			return canvasModule;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private String getCanvasJSONResponse(Request courseRequest, Connector connector, @Nullable String username)
	{
		try( Response response = getCanvasResponse(courseRequest, connector, username) )
		{
			StringWriter sw = new StringWriter();
			CharStreams.copy(new InputStreamReader(response.getInputStream()), sw);
			return sw.toString();
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	private Response getCanvasResponse(Request request, Connector connector, @Nullable String username)
	{
		return getCanvasResponse(request, connector.getAttribute(CanvasConnectorConstants.FIELD_ACCESS_TOKEN),
			username);
	}

	public Response getCanvasResponse(Request request, String accessToken, @Nullable String username)
	{
		setupRequest(request, accessToken, username);

		final Response response = httpService.getWebContent(request, configService.getProxyDetails());
		if( !response.isOk() )
		{
			try
			{
				// try to read the error structure
				try
				{
					final String bodyText = response.getBody();
					final CanvasErrorBean errorBean = jsonMapper.readValue(bodyText, CanvasErrorBean.class);
					final StringBuilder errorMessage = new StringBuilder(
						CurrentLocale.get("com.tle.core.connectors.canvas.error.prelim") + "\n");
					for( CanvasErrorMessageBean message : errorBean.getErrors() )
					{
						errorMessage.append(message.getMessage() + "\n");
					}
					if( response.getCode() == 401 )
					{
						throw new CanvasAuthException(errorMessage.toString());
					}
					else
					{
						if( LOGGER.isDebugEnabled() )
						{
							LOGGER.debug("Error response from Canvas");
							LOGGER.debug(errorMessage.toString());
							LOGGER.debug(bodyText);
						}
						throw new RuntimeException(errorMessage.toString());
					}
				}
				catch( IOException e )
				{
					// Ok, failed at that
					if( response.getCode() == 401 )
					{
						throw new CanvasAuthException(
							CurrentLocale.get("com.tle.core.connectors.canvas.error.unauthorized"));
					}
					else
					{
						final String body = response.getBody();
						throw new RuntimeException(CurrentLocale.get("com.tle.core.connectors.canvas.error.prelim")
							+ " (" + response.getCode() + ") " + response.getMessage() + " --- " + body);
					}
				}
			}
			finally
			{
				try
				{
					response.close();
				}
				catch( IOException io )
				{
					// Don't care
				}
			}
		}
		return response;
	}

	private void setupRequest(Request request, String accessToken, @Nullable String username)
	{
		// Username not used. Need to do OAuth login.
		request.setCharset("utf-8");
		request.addHeader(REQUEST_HEADER_AUTHORIZATION, "Bearer " + accessToken);
	}

	// Link header spec: http://tools.ietf.org/html/rfc5988
	@Nullable
	private Request getNextRequest(Connector connector, @Nullable String username, @Nullable String linksHeader)
	{
		if( linksHeader == null )
		{
			return null;
		}
		final String[] links = linksHeader.split(",");
		for( String linkAndRel : links )
		{
			final String[] linkAndParts = linkAndRel.split(";");
			String link = linkAndParts[0].trim();
			if( link.startsWith("<") )
			{
				link = Utils.safeSubstring(link, 1);
			}
			if( link.endsWith(">") )
			{
				link = Utils.safeSubstring(link, 0, -1);
			}
			for( int i = 1; i < linkAndParts.length; i++ )
			{
				String part = linkAndParts[i].trim();
				if( part.startsWith("rel") )
				{
					String[] relAndValue = part.split("=");
					if( relAndValue.length > 1 )
					{
						String value = relAndValue[1].trim();
						if( value.equals("\"next\"") )
						{
							// use this URL
							Request request = new Request(link);
							return request;
						}
					}
				}
			}
		}
		return null;
	}

	private static class CourseTransformer implements Function<CanvasCourseBean, ConnectorCourse>
	{
		@Override
		public ConnectorCourse apply(CanvasCourseBean canvasCourse)
		{
			final String state = canvasCourse.getState();
			final ConnectorCourse course = new ConnectorCourse(canvasCourse.getId());
			course.setCourseCode(canvasCourse.getCode());
			course.setName(canvasCourse.getName());
			course.setAvailable(state.equals(COURSE_STATE_AVAILABLE));
			return course;
		}
	}

	private static class ModuleTransformer implements Function<CanvasModuleBean, ConnectorFolder>
	{
		private final ConnectorCourse course;

		public ModuleTransformer(ConnectorCourse course)
		{
			this.course = course;
		}

		@Override
		public ConnectorFolder apply(CanvasModuleBean canvasModule)
		{
			final ConnectorFolder folder = new ConnectorFolder(canvasModule.getId(), course);
			folder.setName(canvasModule.getName());
			folder.setAvailable(true);
			folder.setLeaf(true);
			return folder;
		}
	}

	private class ModuleItemTransformer implements Function<CanvasModuleItemBean, ConnectorContent>
	{
		private final Connector connector;
		private final CanvasCourseBean course;
		private final CanvasModuleBean module;

		public ModuleItemTransformer(Connector connector, CanvasCourseBean course, CanvasModuleBean module)
		{
			this.connector = connector;
			this.course = course;
			this.module = module;
		}

		/**
		 * Note: will return null if not an EQUELLA item...
		 */
		@Nullable
		@Override
		public ConnectorContent apply(CanvasModuleItemBean item)
		{
			final ConnectorContent content = new ConnectorContent(
				buildContentId(course.getId(), module.getId(), item.getId()));
			content.setExternalTitle(item.getTitle());
			content.setExternalDescription(null);
			content.setCourse(course.getName());
			content.setFolder(module.getName());

			final String courseUrl = PathUtils.urlPath(connector.getServerUrl(), COURSES,
				httpService.urlPathEncode(course.getId()));
			content.setCourseUrl(courseUrl);
			content.setFolderUrl(PathUtils.urlPath(courseUrl, MODULES) + "#module_" + module.getId());

			// Ugh, I'm going to be sick.
			// We need to parse the URL to extract the UUID and
			// version :(
			// hb image vomit

			final String externalToolUrl = item.getEquellaUrl();
			if( institutionService.isInstitutionUrl(externalToolUrl) )
			{
				final String relUrl = institutionService.removeInstitution(externalToolUrl);
				final String path;
				final String query;
				final int questionIndex = relUrl.lastIndexOf("?");
				if( questionIndex >= 0 )
				{
					path = relUrl.substring(0, questionIndex);
					query = relUrl.substring(questionIndex + 1);
				}
				else
				{
					path = relUrl;
					query = null;
				}

				final String[] parts = path.split("/");
				final List<String> partList = new ArrayList<>();
				for( String part : parts )
				{
					if( !Check.isEmpty(part) )
					{
						partList.add(part);
					}
				}

				if( partList.get(0).equals("integ") )
				{
					final String uuid = partList.get(2);
					final int version = Integer.parseInt(partList.get(3));
					content.setUuid(uuid);
					content.setVersion(version);

					if( query != null )
					{
						final Map<String, String> qs = URLUtils.parseQueryString(query, true);
						final String attachmentUuid = qs.get("attachment.uuid");
						if( attachmentUuid != null )
						{
							content.setAttachmentUuid(attachmentUuid);
						}
					}
				}
				return content;
			}
			return null;
		}
	}

	private static class CanvasModuleItemKey
	{
		private final String courseId;
		private final String moduleId;
		private final String contentId;

		public CanvasModuleItemKey(String courseId, String moduleId, String contentId)
		{
			this.courseId = courseId;
			this.moduleId = moduleId;
			this.contentId = contentId;
		}

		public String getCourseId()
		{
			return courseId;
		}

		public String getModuleId()
		{
			return moduleId;
		}

		public String getContentId()
		{
			return contentId;
		}
	}

	private static class CanvasAuthException extends RuntimeException
	{
		public CanvasAuthException(String message)
		{
			super(message);
		}
	}

	@Override
	public boolean supportsFindUses()
	{
		return false;
	}
}
