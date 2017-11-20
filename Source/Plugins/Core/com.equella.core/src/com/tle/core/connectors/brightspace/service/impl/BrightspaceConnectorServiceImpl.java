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

package com.tle.core.connectors.brightspace.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.tle.core.plugins.AbstractPluginService;
import org.apache.log4j.Logger;

import com.dytech.devlib.Base64;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ViewableItemType;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.Utils;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.common.util.DateHelper;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.connectors.brightspace.BrightspaceAppContext;
import com.tle.core.connectors.brightspace.BrightspaceConnectorConstants;
import com.tle.core.connectors.brightspace.BrightspaceUserContext;
import com.tle.core.connectors.brightspace.beans.AbstractPagedResults.PagingInfo;
import com.tle.core.connectors.brightspace.beans.BrightspaceEquellaLink;
import com.tle.core.connectors.brightspace.beans.BrightspaceLtiLink;
import com.tle.core.connectors.brightspace.beans.BrightspaceQuicklink;
import com.tle.core.connectors.brightspace.beans.BrightspaceUsageResults;
import com.tle.core.connectors.brightspace.beans.BrightspaceWhoAmIUser;
import com.tle.core.connectors.brightspace.beans.ContentObject;
import com.tle.core.connectors.brightspace.beans.CourseOffering;
import com.tle.core.connectors.brightspace.beans.EnrollmentsPagedResults;
import com.tle.core.connectors.brightspace.beans.MyOrgUnitInfo;
import com.tle.core.connectors.brightspace.beans.MyOrgUnitInfo.Access;
import com.tle.core.connectors.brightspace.beans.OrgUnitInfo;
import com.tle.core.connectors.brightspace.beans.RichText;
import com.tle.core.connectors.brightspace.service.BrightspaceConnectorService;
import com.tle.core.connectors.exception.LmsRequiresAuthenticationException;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.AbstractIntegrationConnectorRespository;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Request.Method;
import com.tle.core.services.http.Response;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.integration.Integration.LmsLink;
import com.tle.web.integration.Integration.LmsLinkInfo;
import com.tle.web.selection.SelectedResource;

/*
 * For Brightspace API documentation, go to 
 * 
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(BrightspaceConnectorService.class)
@Singleton
public class BrightspaceConnectorServiceImpl extends AbstractIntegrationConnectorRespository
	implements
		BrightspaceConnectorService
{
	private static final Logger LOGGER = Logger.getLogger(BrightspaceConnectorService.class);

	private static final byte[] SHAREPASS = new byte[]{45, 123, -112, 2, 89, 124, 19, 74, 0, 24, -118, 98, 5, 100, 92,
			7};
	private static final IvParameterSpec INITVEC = new IvParameterSpec("thisis16byteslog".getBytes());

	private static final int TOPIC_TYPE_LINK = 3;

	private static final String API_ROOT = "/d2l/api";
	//"https://private-eac75-equellad2lquicklinks.apiary-mock.com/d2l/api/customization/equellalinks/1.1";
	private static final String CUSTOM_API = API_ROOT + "/customization/equellalinks/1.1";

	private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
	private static final Pattern UUID_VERSION_REGEX = Pattern.compile(
		".*/(" + UUID_REGEX + ")/([0-9])+/?(?:\\?attachment\\.uuid=(" + UUID_REGEX + "))?", Pattern.CASE_INSENSITIVE);
	private static final String KEY_PFX = AbstractPluginService.getMyPluginId(BrightspaceConnectorServiceImpl.class)+".";

	private String leApi;
	private String lpApi;

	@Inject
	@Named("brightspace.api.le.version")
	private String leVersion;
	@Inject
	@Named("brightspace.api.lp.version")
	private String lpVersion;

	@Inject
	private HttpService httpService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private UserSessionService userSessionService;
	@Inject
	private ItemService itemService;

	private Cache<String, CourseOffering> courseCache = CacheBuilder.newBuilder()
		.expireAfterAccess(30, TimeUnit.MINUTES).build();
	private Cache<String, ContentObject> moduleCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES)
		.build();

	private CourseOfferingTransformer courseOfferingTransformer = new CourseOfferingTransformer();
	private static final ObjectMapper jsonMapper = new ObjectMapper();
	private static final ObjectMapper prettyJsonMapper = new ObjectMapper();

	static
	{
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.setSerializationInclusion(Include.NON_NULL);

		prettyJsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		prettyJsonMapper.setSerializationInclusion(Include.NON_NULL);
		prettyJsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@PostConstruct
	public void init()
	{
		leApi = API_ROOT + "/le/" + leVersion;
		lpApi = API_ROOT + "/lp/" + lpVersion;
	}

	@Override
	public String whoAmI(String appId, String appKey, String brightspaceServerUrl, String userId, String userKey)
	{
		final BrightspaceAppContext appContext = getAppContext(appId, appKey, brightspaceServerUrl);
		final BrightspaceUserContext userContext = appContext.createUserContext(userId, userKey);

		final URI uri = userContext.createAuthenticatedUri(lpApi + "/users/whoami", "GET");
		final Request request = new Request(uri.toString());
		if( LOGGER.isTraceEnabled() )
		{
			LOGGER.trace("GET to Brightspace: " + request.getUrl());
		}

		final Response webContent = httpService.getWebContent(request, configService.getProxyDetails());
		final String body = webContent.getBody();
		final int code = webContent.getCode();

		if( LOGGER.isTraceEnabled() )
		{
			LOGGER.trace("Received from Brightspace (" + code + "):");
			LOGGER.trace(prettyJson(body));
		}
		if( code >= 300 )
		{
			throw new RuntimeException("Received " + code + " from Brightspace. Body = " + body);
		}
		try
		{
			final BrightspaceWhoAmIUser user = jsonMapper.readValue(body, BrightspaceWhoAmIUser.class);

			return user.getUniqueName();
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String testApplication(String appId, String appKey, String brightspaceServerUrl)
	{
		BrightspaceAppContext brightspaceAppContext = new BrightspaceAppContext(appId, appKey, brightspaceServerUrl);
		URI uri = brightspaceAppContext.createAnonymousUserContext().createAuthenticatedUri(API_ROOT + "/versions/",
			"GET");

		Response webContent = httpService.getWebContent(new Request(uri.toString()), configService.getProxyDetails());
		if( webContent.getCode() == 200 )
		{
			return "ok";
		}
		return "fail";
	}

	@Override
	public boolean isRequiresAuthentication(Connector connector)
	{
		try
		{
			getUserContext(connector);
			return false;
		}
		catch( LmsRequiresAuthenticationException ex )
		{
			return true;
		}
	}

	@Override
	public String getAuthorisationUrl(Connector connector, String forwardUrl, @Nullable String authData)
	{
		final BrightspaceAppContext appContext = getAppContext(connector);
		return getAuthorisationUrl(appContext, forwardUrl, authData);
	}

	@Override
	public String getAuthorisationUrl(String appId, String appKey, String brightspaceServerUrl, String forwardUrl,
		@Nullable String postfixKey)
	{
		final BrightspaceAppContext appContext = getAppContext(appId, appKey, brightspaceServerUrl);
		return getAuthorisationUrl(appContext, forwardUrl, postfixKey);
	}

	private String getAuthorisationUrl(BrightspaceAppContext appContext, String forwardUrl, @Nullable String postfixKey)
	{
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode stateJson = mapper.createObjectNode();
		stateJson.put(BrightspaceConnectorConstants.STATE_KEY_FORWARD_URL, forwardUrl);
		if( postfixKey != null )
		{
			stateJson.put(BrightspaceConnectorConstants.STATE_KEY_POSTFIX_KEY, postfixKey);
		}

		URI uri;
		try
		{
			uri = appContext.createWebUrlForAuthentication(
				URI.create(institutionService.institutionalise(BrightspaceConnectorConstants.AUTH_URL)),
				encrypt(mapper.writeValueAsString(stateJson)));
		}
		catch( JsonProcessingException e )
		{
			throw Throwables.propagate(e);
		}
		return uri.toString();
	}

	@Nullable
	@Override
	public String getCourseCode(Connector connector, String username, String courseId) throws LmsUserNotFoundException
	{
		// Assumed not admin?
		final BrightspaceUserContext userContext = getUserContext(connector);
		final CourseOffering course = getBrightspaceCourse(connector, courseId, userContext);
		return course.getCode();
	}

	@Override
	public ConnectorCourse getCourse(Connector connector, String courseId)
	{
		// Assumed not admin?
		final BrightspaceUserContext userContext = getUserContext(connector);
		return courseOfferingTransformer.apply(getBrightspaceCourse(connector, courseId, userContext));
	}

	@Override
	public List<ConnectorCourse> getCourses(Connector connector, String username, boolean editableOnly,
		boolean archived, final boolean management) throws LmsUserNotFoundException
	{
		final List<ConnectorCourse> list = new ArrayList<>();

		final BrightspaceUserContext userContext = (management ? getAdminContext(connector)
			: getUserContext(connector));

		String url = lpApi + "/enrollments/myenrollments/?orgUnitTypeId=3";
		if( !archived )
		{
			url += "&active=true";
		}
		boolean hasMore = true;
		int giveUp = 0;
		String bookmark = null;
		while( hasMore && giveUp < 10 )
		{
			String pagedUrl = url;
			if( bookmark != null )
			{
				pagedUrl += "&bookmark=" + bookmark;
			}
			final EnrollmentsPagedResults results = sendBrightspaceData(connector, pagedUrl,
				EnrollmentsPagedResults.class, null, userContext, Method.GET);
			final PagingInfo pagingInfo = results.getPagingInfo();
			hasMore = pagingInfo.isHasMoreItems();
			bookmark = pagingInfo.getBookmark();
			giveUp++;

			final List<MyOrgUnitInfo> items = results.getItems();
			for( MyOrgUnitInfo item : items )
			{
				final Access access = item.getAccess();
				// It probably shouldn't be null, but hey, best be sure
				if( access != null )
				{
					if( access.isActive() || archived )
					{
						final OrgUnitInfo ou = item.getOrgUnit();
						final ConnectorCourse cc = new ConnectorCourse(Long.toString(ou.getId()));
						cc.setCourseCode(ou.getCode());
						cc.setName(ou.getName());
						cc.setAvailable(access.isActive());
						list.add(cc);
					}
				}
			}
		}

		return list;
	}

	/**
	 * @return Brightspace Modules
	 */
	@Override
	public List<ConnectorFolder> getFoldersForCourse(Connector connector, String username, String courseId,
		boolean management) throws LmsUserNotFoundException
	{
		final BrightspaceUserContext userContext = (management ? getAdminContext(connector)
			: getUserContext(connector));
		final URI modules = userContext.createAuthenticatedUri(leApi + "/" + courseId + "/content/root/", "GET");
		final Request request = new Request(modules.toString());
		try( Response webContent = httpService.getWebContent(request, configService.getProxyDetails()) )
		{
			final ConnectorCourse course = new ConnectorCourse(courseId);

			final String stringed = webContent.getBody();
			final List<ContentObject> contents = jsonMapper.readValue(stringed, new TypeReference<List<ContentObject>>()
			{
			});
			final List<ConnectorFolder> folders = new ArrayList<>();
			for( ContentObject content : contents )
			{
				//Don't include topics
				if( content.getType() == ContentObject.TYPE_MODULE )
				{
					ConnectorFolder folder = new ConnectorFolder(Long.toString(content.getId()), course);
					folder.setName(content.getTitle());
					folders.add(folder);
				}
			}
			return folders;
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
	}

	@Override
	public List<ConnectorFolder> getFoldersForFolder(Connector connector, String username, String courseId,
		String folderId, boolean management) throws LmsUserNotFoundException
	{
		// Assumed not admin?
		final BrightspaceUserContext userContext = (management ? getAdminContext(connector)
			: getUserContext(connector));
		final ModuleTransformer transformer = new ModuleTransformer(
			courseOfferingTransformer.apply(getBrightspaceCourse(connector, courseId, userContext)));
		final List<ConnectorFolder> folders = new ArrayList<>();
		final ContentObject module = getModule(connector, courseId, folderId, userContext);
		for( ContentObject content : module.getStructure() )
		{
			//Don't include topics
			if( content.getType() == ContentObject.TYPE_MODULE )
			{
				folders.add(transformer.apply(content));
			}
		}
		return folders;
	}

	@Override
	public List<ConnectorContent> findUsages(Connector connector, String username, String uuid, int version,
		boolean versionIsLatest, boolean archived, boolean allVersion) throws LmsUserNotFoundException
	{
		final List<ConnectorContent> usages = new ArrayList<>();
		final BrightspaceUserContext userContext = getUserContext(connector);

		// Get version 0 links
		if( allVersion || versionIsLatest )
		{
			appendFindUsages(userContext, usages, connector, uuid, 0, archived);
		}

		// Get all other version links
		if( allVersion )
		{
			int v = itemService.getLatestVersion(uuid);
			while( v > 0 )
			{
				appendFindUsages(userContext, usages, connector, uuid, v, archived);
				v--;
			}
		}
		else
		{
			appendFindUsages(userContext, usages, connector, uuid, version, archived);
		}

		return usages;
	}

	private void appendFindUsages(BrightspaceUserContext userContext, List<ConnectorContent> usages,
		Connector connector, String uuid, int version, boolean archived)
	{
		final StringBuilder url = new StringBuilder(CUSTOM_API + "/linkinfo/" + uuid + "/" + version + "/?limit=1000");
		if( archived )
		{
			url.append("&showInactive=" + archived);
		}

		final BrightspaceUsageResults results = sendBrightspaceData(connector, url.toString(),
			BrightspaceUsageResults.class, null, userContext, Method.GET);

		final BrightspaceEquellaLink[] objects = results.getObjects();
		for( BrightspaceEquellaLink link : objects )
		{
			//if( link.getVisible() || archived )
			//{
			usages.add(convertUsage(link));
			//}
		}
	}

	@Override
	public SearchResults<ConnectorContent> findAllUsages(Connector connector, String username, @Nullable String query,
		@Nullable String courseId, @Nullable String folderId, boolean archived, int offset, int count,
		ExternalContentSortType sortType, boolean reverseSort) throws LmsUserNotFoundException
	{
		final List<ConnectorContent> usages = new ArrayList<>();

		// Assumed admin
		final BrightspaceUserContext userContext = getAdminContext(connector);

		final StringBuilder qs = new StringBuilder("?");
		qs.append("offset=").append(offset);
		qs.append("&limit=").append(count);
		qs.append("&sort=").append(convertFindAllUsagesSort(sortType));
		if( !Strings.isNullOrEmpty(query) )
		{
			qs.append("&search=").append(URLUtils.basicUrlEncode(query));
		}
		if( !Strings.isNullOrEmpty(courseId) )
		{
			qs.append("&orgunitid=").append(courseId);
		}
		if( !Strings.isNullOrEmpty(folderId) )
		{
			qs.append("&parentmoduleid=").append(folderId);
		}
		if( archived )
		{
			qs.append("&showInactive=" + archived);
		}
		final BrightspaceUsageResults results = sendBrightspaceData(connector, CUSTOM_API + "/links/" + qs.toString(),
			BrightspaceUsageResults.class, null, userContext, Method.GET);
		final BrightspaceEquellaLink[] objects = results.getObjects();
		for( BrightspaceEquellaLink link : objects )
		{
			//if( link.getVisible() || archived )
			//{
			usages.add(convertUsage(link));
			//}
		}
		return new SimpleSearchResults<ConnectorContent>(usages, usages.size(), offset, results.getResultSetCount());
	}

	private String convertFindAllUsagesSort(ExternalContentSortType sortType)
	{
		switch( sortType )
		{
			case COURSE:
				return "OrgUnitName";
			case DATE_ADDED:
				return "LastModifiedDate";
			case NAME:
				return "TopicName";
			default:
				//unpossible
				return "LastModifiedDate";
		}
	}

	private ConnectorContent convertUsage(BrightspaceEquellaLink link)
	{
		// ID is construct of course + _ + module + _ + topic + _ + lti link
		final ConnectorContent usage = new ConnectorContent(
			new CompositeId(Integer.toString(link.getOrgUnitId()), Integer.toString(link.getParentModuleId()),
				Integer.toString(link.getTopicId()), Integer.toString(link.getLtiLinkId())).toString());
		usage.setCourse(link.getOrgUnitName());
		usage.setCourseCode(link.getOrgUnitCode());
		usage.setAvailable(link.getVisible());
		usage.setExternalTitle(link.getTopicName());
		usage.setFolderUrl(link.getD2lTopicLink());
		usage.setFolder(link.getParentModuleName());
		// Brightspace have no date added field
		final UtcDate modDate = DateHelper.parseOrNull(link.getLastModifiedDate(), Dates.ISO_WITH_MILLIS_NO_TIMEZONE);
		if( modDate != null )
		{
			usage.setDateAdded(modDate.toDate());
		}

		final String linkTarget = link.getLtiLink();
		if( linkTarget != null )
		{
			final Matcher matcher = UUID_VERSION_REGEX.matcher(linkTarget);
			if( matcher.matches() )
			{
				usage.setUuid(matcher.group(1));
				usage.setVersion(Integer.parseInt(matcher.group(2)));
				// I don't trust the regex enough to catch all cases of query strings...
				if( linkTarget.contains("?") )
				{
					Map<String, String> params = URLUtils
						.parseQueryString(Utils.safeSubstring(linkTarget, linkTarget.indexOf("?")), true);
					usage.setAttachmentUuid(params.get("attachment.uuid"));
				}
			}
		}
		usage.setAttribute("contentAvailable", getKey("bspace.finduses.label.visible"),
			CurrentLocale.get(getKey("bspace.finduses.value.visible." + (link.getVisible() ? "yes" : "no"))));

		return usage;
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
		final BrightspaceUserContext userContext = getAdminContext(connector);
		final CompositeId compId = new CompositeId(contentId);
		delete(userContext, connector, compId);
		return true;
	}

	@Override
	public boolean editContent(Connector connector, String username, String contentId, String title, String description)
		throws LmsUserNotFoundException
	{
		final BrightspaceUserContext userContext = getAdminContext(connector);
		final CompositeId compId = new CompositeId(contentId);
		final ContentObject contentObject = sendBrightspaceData(connector,
			leApi + "/" + compId.getCourseId() + "/content/topics/" + compId.getTopicId(), ContentObject.class, null,
			userContext, Method.GET);

		contentObject.setTitle(title);
		if( !Strings.isNullOrEmpty(description) )
		{
			final RichText desc = new RichText();
			desc.setContent(description);
			desc.setType("Text");
			contentObject.setDescription(desc);
		}
		sendBrightspaceData(connector, leApi + "/" + compId.getCourseId() + "/content/topics/" + compId.getTopicId(),
			null, contentObject, userContext, Method.PUT);
		return true;
	}

	@Override
	public boolean moveContent(Connector connector, String username, String contentId, String courseId, String folderId)
		throws LmsUserNotFoundException
	{
		final BrightspaceUserContext userContext = getAdminContext(connector);
		final CompositeId compId = new CompositeId(contentId);
		final ContentObject contentObject = sendBrightspaceData(connector,
			leApi + "/" + compId.getCourseId() + "/content/topics/" + compId.getTopicId(), ContentObject.class, null,
			userContext, Method.GET);
		final BrightspaceLtiLink ltiLink = sendBrightspaceData(connector,
			leApi + "/lti/link/" + compId.getCourseId() + "/" + compId.getLtiLinkId(), BrightspaceLtiLink.class, null,
			userContext, Method.GET);

		// Insert new
		final RichText oldDescription = contentObject.getDescription();
		final String description = oldDescription == null ? "" : oldDescription.getContent();
		addQuicklink(connector, courseId, folderId, contentObject.getTitle(), description, ltiLink.getUrl(),
			TopicCreationOption.CREATE, userContext);

		// Delete old
		delete(userContext, connector, compId);
		return true;
	}

	private void delete(BrightspaceUserContext userContext, Connector connector, CompositeId compId)
	{
		sendBrightspaceData(connector, leApi + "/" + compId.getCourseId() + "/content/topics/" + compId.getTopicId(),
			null, null, userContext, Method.DELETE);
		sendBrightspaceData(connector, leApi + "/lti/link/" + compId.getLtiLinkId(), null, null, userContext,
			Method.DELETE);
	}

	private String getKey(String partKey)
	{
		return KEY_PFX + partKey;
	}

	@Override
	public ConnectorTerminology getConnectorTerminology()
	{
		final ConnectorTerminology terms = new ConnectorTerminology();
		terms.setShowArchived(getKey("bspace.finduses.showarchived"));
		terms.setShowArchivedLocations(getKey("bspace.finduses.showarchived.courses"));
		terms.setCourseHeading(getKey("bspace.finduses.course"));
		terms.setLocationHeading(getKey("bspace.finduses.location"));
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
	public boolean supportsFindUses()
	{
		return true;
	}

	@Override
	public boolean supportsReverseSort()
	{
		return false;
	}

	@Override
	public boolean supportsEditDescription()
	{
		return false;
	}

	@Override
	protected String getIntegrationId()
	{
		return "gen";
	}

	@Override
	protected boolean isRelativeUrls()
	{
		// We require a full URL for Brightspace content
		return false;
	}

	/**
	 * @param folderId This is the Brightspace Module ID
	 */
	@Override
	public ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String folderId,
		IItem<?> item, SelectedResource selectedResource) throws LmsUserNotFoundException
	{
		// Assumed not admin?
		final BrightspaceUserContext userContext = getUserContext(connector);
		final LmsLinkInfo linkInfo = getLmsLink(item, selectedResource);
		final LmsLink lmsLink = linkInfo.getLmsLink();
		addQuicklink(connector, courseId, folderId, lmsLink.getName(), lmsLink.getDescription(), lmsLink.getUrl(),
			TopicCreationOption.CREATE, userContext);

		return new ModuleTransformer(
			courseOfferingTransformer.apply(getBrightspaceCourse(connector, courseId, userContext)))
				.apply(getModule(connector, courseId, folderId, userContext));
	}

	@Override
	protected ViewableItemType getViewableItemType()
	{
		return ViewableItemType.GENERIC;
	}

	@Override
	public BrightspaceQuicklink addQuicklink(Connector connector, String courseId, String moduleId, LmsLink link,
		TopicCreationOption topicOption)
	{
		// Assumed not admin?
		final BrightspaceUserContext userContext = getUserContext(connector);
		return addQuicklink(connector, courseId, moduleId, link.getName(), link.getDescription(), link.getUrl(),
			topicOption, userContext);
	}

	private BrightspaceQuicklink addQuicklink(Connector connector, String courseId, String moduleId, String name,
		String description, String url, TopicCreationOption topicOption, BrightspaceUserContext userContext)
	{
		final BrightspaceLtiLink ltiLink = new BrightspaceLtiLink();
		ltiLink.setTitle(name);
		ltiLink.setUrl(url);
		ltiLink.setDescription(description);
		ltiLink.setKey("");
		ltiLink.setPlainSecret("");
		ltiLink.setVisible(true);
		ltiLink.setSignMessage(true);
		ltiLink.setSignWithTc(true);
		ltiLink.setSendTcInfo(true);
		ltiLink.setSendContextInfo(true);
		ltiLink.setSendUserId(true);
		ltiLink.setSendUserName(true);
		ltiLink.setSendUserEmail(true);
		ltiLink.setSendLinkTitle(true);
		ltiLink.setSendLinkDescription(true);
		ltiLink.setSendD2LUserName(true);
		ltiLink.setSendD2LOrgDefinedId(false);
		ltiLink.setSendD2LOrgRoleId(false);

		// Add the LTI link as the admin context regardless of the supplied userContext
		final BrightspaceLtiLink retLink = sendBrightspaceData(connector, leApi + "/lti/link/" + courseId,
			BrightspaceLtiLink.class, ltiLink, getAdminContext(connector), Method.POST);

		final BrightspaceQuicklink quicklink = sendBrightspaceData(connector,
			leApi + "/lti/quicklink/" + courseId + "/" + retLink.getLtiLinkId(), BrightspaceQuicklink.class, null,
			userContext, Method.POST);

		if( topicOption == TopicCreationOption.CREATE )
		{
			final ContentObject topic = new ContentObject();
			topic.setType(ContentObject.TYPE_TOPIC);
			topic.setTopicType(TOPIC_TYPE_LINK);
			topic.setTitle(name);
			topic.setShortTitle("");
			topic.setUrl(quicklink.getPublicUrl());
			topic.setHidden(false);
			topic.setLocked(false);

			sendBrightspaceData(connector, leApi + "/" + courseId + "/content/modules/" + moduleId + "/structure/",
				null, topic, userContext, Method.POST);
		}
		return quicklink;
	}

	@Override
	public String encrypt(String data)
	{
		if( !Check.isEmpty(data) )
		{
			try
			{
				SecretKey key = new SecretKeySpec(SHAREPASS, "AES");
				Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				ecipher.init(Cipher.ENCRYPT_MODE, key, INITVEC);

				// Encrypt
				byte[] enc = ecipher.doFinal(data.getBytes());
				return new Base64().encode(enc);

			}
			catch( Exception e )
			{
				throw new RuntimeException("Error encrypting", e);
			}
		}

		return data;
	}

	@Override
	public String decrypt(String encryptedData)
	{
		if( !Check.isEmpty(encryptedData) )
		{
			try
			{
				byte[] bytes = new Base64().decode(encryptedData);
				SecretKey key = new SecretKeySpec(SHAREPASS, "AES");
				Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				ecipher.init(Cipher.DECRYPT_MODE, key, INITVEC);
				return new String(ecipher.doFinal(bytes));
			}
			catch( Exception e )
			{
				throw new RuntimeException("Error decrypting ", e);
			}
		}

		return encryptedData;
	}

	private BrightspaceAppContext getAppContext(Connector connector)
	{
		return getAppContext(connector.getAttribute(BrightspaceConnectorConstants.FIELD_APP_ID),
			connector.getAttribute(BrightspaceConnectorConstants.FIELD_APP_KEY), connector.getServerUrl());
	}

	private BrightspaceAppContext getAppContext(String appId, String appKey, String serverUrl)
	{
		return new BrightspaceAppContext(appId, appKey, serverUrl);
	}

	private BrightspaceUserContext getUserContext(Connector connector)
	{
		BrightspaceUserContext userContext = userSessionService
			.getAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_CONTEXT);
		if( userContext == null )
		{
			final String userId = userSessionService.getAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_ID);
			if( userId == null )
			{
				throw new LmsRequiresAuthenticationException("No Brightspace user ID associated with session");
			}

			final String userKey = userSessionService.getAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_KEY);

			final BrightspaceAppContext appContext = getAppContext(connector);
			userContext = appContext.createUserContext(userId, userKey);
			userSessionService.setAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_CONTEXT, userContext);

			userSessionService.removeAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_ID);
			userSessionService.removeAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_KEY);
		}
		return userContext;
	}

	private BrightspaceUserContext getAdminContext(Connector connector)
	{
		final String userId = connector.getAttribute(BrightspaceConnectorConstants.FIELD_ADMIN_USER_ID);
		final String userKey = connector.getAttribute(BrightspaceConnectorConstants.FIELD_ADMIN_USER_KEY);
		if( Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(userKey) )
		{
			throw new LmsRequiresAuthenticationException(
				CurrentLocale.get("com.tle.core.connectors.brightspace.error.noadmin"));
		}

		final BrightspaceAppContext appContext = getAppContext(connector);
		return appContext.createUserContext(userId, decrypt(userKey));
	}

	@Nullable
	private <T> T sendBrightspaceData(Connector connector, String path, @Nullable Class<T> returnType,
		@Nullable Object data, BrightspaceUserContext userContext, Method method)
	{
		try
		{
			final URI uri;
			if( !path.startsWith("http") )
			{
				uri = userContext.createAuthenticatedUri(path, method.toString());
			}
			else
			{
				uri = URI.create(path);
			}
			final Request request = new Request(uri.toString());
			request.setMethod(method);
			if( LOGGER.isTraceEnabled() )
			{
				LOGGER.trace(method + " to Brightspace: " + request.getUrl());
			}

			final String body;
			if( data != null )
			{
				body = jsonMapper.writeValueAsString(data);
			}
			else
			{
				body = "";
			}
			request.setBody(body);
			if( body.length() > 0 )
			{
				request.addHeader("Content-Type", "application/json");
			}
			if( LOGGER.isTraceEnabled() )
			{
				LOGGER.trace("Sending " + prettyJson(body));
			}

			try( Response response = httpService.getWebContent(request, configService.getProxyDetails()) )
			{
				final String responseBody = response.getBody();
				final int code = response.getCode();
				if( LOGGER.isTraceEnabled() )
				{
					LOGGER.trace("Received from Brightspace (" + code + "):");
					LOGGER.trace(prettyJson(responseBody));
				}
				if( code >= 300 )
				{
					throw new RuntimeException("Received " + code + " from Brightspace. Body = " + responseBody);
				}
				if( returnType != null )
				{
					final T content = jsonMapper.readValue(responseBody, returnType);
					return content;
				}
				return null;
			}
			catch( IOException io )
			{
				throw Throwables.propagate(io);
			}
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
	}

	private ContentObject getModule(Connector connector, String courseId, String folderId,
		BrightspaceUserContext userContext)
	{
		final String key = CurrentInstitution.get().getUniqueId() + ":" + connector.getUuid() + ":" + courseId + ":"
			+ folderId;
		ContentObject module = moduleCache.getIfPresent(key);
		if( module == null )
		{
			module = sendBrightspaceData(connector, leApi + "/" + courseId + "/content/modules/" + folderId,
				ContentObject.class, null, userContext, Method.GET);
			moduleCache.put(key, module);
		}
		return module;
	}

	private CourseOffering getBrightspaceCourse(Connector connector, String courseId,
		BrightspaceUserContext userContext)
	{
		final String key = CurrentInstitution.get().getUniqueId() + ":" + connector.getUuid() + ":" + courseId;
		CourseOffering course = courseCache.getIfPresent(key);
		if( course == null )
		{
			course = sendBrightspaceData(connector, lpApi + "/courses/" + courseId, CourseOffering.class, null,
				userContext, Method.GET);
			courseCache.put(key, course);
		}
		return course;
	}

	protected static class CompositeId
	{
		private String courseId;
		private String moduleId;
		private String topicId;
		private String ltiLinkId;

		public CompositeId(String composedId)
		{
			final String[] bits = composedId.split("_");
			if( bits.length != 4 )
			{
				throw new RuntimeException("Composite ID not composed of 4 parts");
			}

			this.courseId = bits[0];
			this.moduleId = bits[1];
			this.topicId = bits[2];
			this.ltiLinkId = bits[3];
		}

		public CompositeId(String courseId, String moduleId, String topicId, String ltiLinkId)
		{
			this.courseId = courseId;
			this.moduleId = moduleId;
			this.topicId = topicId;
			this.ltiLinkId = ltiLinkId;
		}

		public String getCourseId()
		{
			return courseId;
		}

		public void setCourseId(String courseId)
		{
			this.courseId = courseId;
		}

		public String getModuleId()
		{
			return moduleId;
		}

		public void setModuleId(String moduleId)
		{
			this.moduleId = moduleId;
		}

		public String getTopicId()
		{
			return topicId;
		}

		public void setTopicId(String topicId)
		{
			this.topicId = topicId;
		}

		public String getLtiLinkId()
		{
			return ltiLinkId;
		}

		public void setLtiLinkId(String ltiLinkId)
		{
			this.ltiLinkId = ltiLinkId;
		}

		@Override
		public String toString()
		{
			return String.join("_", courseId, moduleId, topicId, ltiLinkId);
		}
	}

	@Nullable
	private String prettyJson(@Nullable String json)
	{
		if( Strings.isNullOrEmpty(json) )
		{
			return json;
		}
		try
		{
			return prettyJsonMapper.writeValueAsString(prettyJsonMapper.readTree(json));
		}
		catch( IOException io )
		{
			return json;
		}
	}

	private static class CourseOfferingTransformer implements Function<CourseOffering, ConnectorCourse>
	{
		@Override
		public ConnectorCourse apply(CourseOffering course)
		{
			final ConnectorCourse connCourse = new ConnectorCourse(course.getId());
			connCourse.setCourseCode(course.getCode());
			connCourse.setName(course.getName());
			return connCourse;
		}
	}

	private static class ModuleTransformer implements Function<ContentObject, ConnectorFolder>
	{
		private final ConnectorCourse course;

		public ModuleTransformer(ConnectorCourse course)
		{
			this.course = course;
		}

		@Override
		public ConnectorFolder apply(ContentObject module)
		{
			final ConnectorFolder folder = new ConnectorFolder(Long.toString(module.getId()), course);
			folder.setName(module.getTitle());
			Boolean hidden = module.getHidden();
			folder.setAvailable(hidden == null || !hidden);
			return folder;
		}
	}
}
