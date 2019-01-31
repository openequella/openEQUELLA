package com.tle.core.connectors.blackboard.service.impl;

import com.dytech.devlib.Base64;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ViewableItemType;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.searching.SearchResults;
import com.tle.common.util.BlindSSLSocketFactory;
import com.tle.core.connectors.blackboard.beans.*;
import com.tle.core.connectors.blackboard.service.BlackboardConnectorService;
import com.tle.core.connectors.blackboard.service.BlackboardRESTConnectorService;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.AbstractIntegrationConnectorRespository;
import com.tle.core.connectors.service.ConnectorRepositoryImplementation;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.guice.Bindings;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.selection.SelectedResource;
import net.sf.json.JSONObject;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@NonNullByDefault
@SuppressWarnings({"nls", "deprecation"})
@Bind(BlackboardRESTConnectorService.class)
@Singleton
public class BlackboardRESTConnectorServiceImpl extends AbstractIntegrationConnectorRespository implements BlackboardRESTConnectorService
{
	private static final Logger LOGGER = Logger.getLogger(BlackboardRESTConnectorService.class);

	private static final String API_ROOT = "/learn/api/public";

	private String bbApi;

	@Inject
	@Named("blackboard.api.version")
	private String bbApiVersion;
	@Inject
	private HttpService httpService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ConnectorService connectorService;

	private InstitutionCache<LoadingCache<String, LoadingCache<String, String>>> tokenCache;

	private static final ObjectMapper jsonMapper = new ObjectMapper();
	private static final ObjectMapper prettyJsonMapper = new ObjectMapper();

	static
	{
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		prettyJsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		prettyJsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		prettyJsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	//FIXME: these would be from config (need a new UI to store them)
	// As it stands, you can inject these via Java startup parameters
	// -Dbb.api.key=xxx -Dbb.api.secret=yyy
	private static String BB_API_KEY = null;
	private static String BB_API_SECRET = null;

	public BlackboardRESTConnectorServiceImpl()
	{
		// Ewwww
		BlindSSLSocketFactory.register();
		// Turn off spurious Pre-emptive Authentication bollocks
		Logger.getLogger("org.apache.commons.httpclient.HttpMethodDirector").setLevel(Level.ERROR);

		BB_API_KEY = System.getProperty("bb.api.key");
		BB_API_SECRET = System.getProperty("bb.api.secret");
	}

	@PostConstruct
	public void init()
	{
		bbApi = API_ROOT + "/v" + bbApiVersion;
	}

	@Inject
	public void setInstitutionService(InstitutionService service)
	{
		tokenCache = service.newInstitutionAwareCache(new CacheLoader<Institution, LoadingCache<String, LoadingCache<String, String>>>()
		{
			@Override
			public LoadingCache<String, LoadingCache<String, String>> load(Institution key)
			{
				// MaximumSize is set to 200, which would allow for 200 Blackboard REST connectors, which should be more than enough for anyone.
				return CacheBuilder.newBuilder().maximumSize(200).expireAfterAccess(60, TimeUnit.MINUTES)
					.build(new CacheLoader<String,  LoadingCache<String,String>>()
					{
						@Override
						public LoadingCache<String,String> load(final String connectorUuid) throws Exception
						{
							// BB tokens last one hour, so no point holding onto it longer than that. Of course, we need to handle the case
							// where we are still holding onto an expired token.

							return CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES)
								.build(new CacheLoader<String, String>()
								{
									@Override
									public String load(String fixedKey)
									{
										// fixedKey is ignored. It's alwasy TOKEN
										final Connector connector = connectorService.getByUuid(connectorUuid);
										final String b64 = new Base64().encode((BB_API_KEY + ":" + BB_API_SECRET).getBytes())
											.replace("\n", "").replace("\r", "");

										//FIXME: check for ending slash
										final Request req = new Request(connector.getServerUrl() + "/learn/api/public/v1/oauth2/token");
										req.setMethod(Request.Method.POST);
										req.setMimeType("application/x-www-form-urlencoded");
										req.addHeader("Authorization", "Basic " + b64);
										req.setBody("grant_type=client_credentials");
										try (final Response resp = httpService.getWebContent(req, configService.getProxyDetails()))
										{
											final Token token = jsonMapper.readValue(resp.getInputStream(), Token.class);
											return token.getAccessToken();
										}
										catch( Exception e )
										{
											throw Throwables.propagate(e);
										}
									}
								});
						}
					});
			}
		});
	}

	@Override
	protected ViewableItemType getViewableItemType()
	{
		return null;
	}

	@Override
	protected String getIntegrationId()
	{
		return null;
	}

	@Override
	protected boolean isRelativeUrls()
	{
		return false;
	}

	@Override
	public boolean isRequiresAuthentication(Connector connector)
	{
		return false;
	}

	@Override
	public String getAuthorisationUrl(Connector connector, String forwardUrl, String authData)
	{
		return null;
	}

	@Override
	public String getCourseCode(Connector connector, String username, String courseId) throws LmsUserNotFoundException
	{
		return null;
	}

	@Override
	public List<ConnectorCourse> getCourses(Connector connector, String username, boolean editableOnly, boolean archived, boolean management) throws LmsUserNotFoundException
	{
		final List<ConnectorCourse> list = new ArrayList<>();

		// FIXME: courses for current user...?
		String url = bbApi + "/courses";
		/*
		if( !archived )
		{
			url += "&active=true";
		}*/
//		boolean hasMore = true;
//		int giveUp = 0;
//		String bookmark = null;
//		while( hasMore && giveUp < 10 )
//		{
//			String pagedUrl = url;
//			if( bookmark != null )
//			{
//				pagedUrl += "&bookmark=" + bookmark;
//			}
			final Courses courses = sendBlackboardData(connector, url,
				Courses.class, null, Request.Method.GET);
			//final AbstractPagedResults.PagingInfo pagingInfo = results.getPagingInfo();
			//hasMore = pagingInfo.isHasMoreItems();
			//bookmark = pagingInfo.getBookmark();
			//giveUp++;

			final List<Course> results = courses.getResults();
			for( Course course : results )
			{
				// FIXME: filter only available courses depending on archived parameter
				final ConnectorCourse cc = new ConnectorCourse(course.getId());
				cc.setCourseCode(course.getCourseId());
				cc.setName(course.getName());
				cc.setAvailable("Yes".equals(course.getAvailability().getAvailable()));
				list.add(cc);
//				final MyOrgUnitInfo.Access access = item.getAccess();
//				// It probably shouldn't be null, but hey, best be sure
//				if( access != null )
//				{
//					if( access.isActive() || archived )
//					{
//						final OrgUnitInfo ou = item.getOrgUnit();
//						final ConnectorCourse cc = new ConnectorCourse(Long.toString(ou.getId()));
//						cc.setCourseCode(ou.getCode());
//						cc.setName(ou.getName());
//						cc.setAvailable(access.isActive());
//						list.add(cc);
//					}
//				}
			}
//		}

		return list;
	}

	@Override
	public List<ConnectorFolder> getFoldersForCourse(Connector connector, String username, String courseId, boolean management) throws LmsUserNotFoundException
	{
		final List<ConnectorFolder> list = new ArrayList<>();

		// FIXME: courses for current user...?
		final String url = bbApi + "/courses/" + courseId + "/contents";

		final Contents contents = sendBlackboardData(connector, url,
			Contents.class, null, Request.Method.GET);
		final ConnectorCourse course = new ConnectorCourse(courseId);
		final List<Content> results = contents.getResults();
		for( Content content : results )
		{
			final Content.ContentHandler handler = content.getContentHandler();
			if (handler != null && "resource/x-bb-folder".equals(handler.getId()))
			{
				// FIXME: filter only user visible folders?
				final ConnectorFolder cc = new ConnectorFolder(content.getId(), course);
				// TODO: null safe it
				cc.setAvailable("Yes".equals(content.getAvailability().getAvailable()));
				cc.setName(content.getTitle());
				cc.setLeaf(content.getHasChildren() != null && !content.getHasChildren());
				//cc.setModifiedDate(content.getCreated());
				list.add(cc);
			}
		}

		return list;
	}

	@Override
	public List<ConnectorFolder> getFoldersForFolder(Connector connector, String username, String courseId, String folderId, boolean management) throws LmsUserNotFoundException
	{
		final List<ConnectorFolder> list = new ArrayList<>();
		return list;
	}

	@Override
	public ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String folderId, IItem<?> item, SelectedResource selectedResource) throws LmsUserNotFoundException
	{
		final List<ConnectorFolder> list = new ArrayList<>();

		final String url = bbApi + "/courses/" + courseId + "/contents/" + folderId;

		JSONObject jsonData = new JSONObject();
		jsonData.put("title", "A TP link from oE! - " + selectedResource.getTitle());
		//FIXME Only set parentId for non-top level content
		//jsonData.put("parentId", folderId);
		jsonData.put("body", selectedResource.getDescription());

		JSONObject jsonAvailabilityData = new JSONObject();
		jsonAvailabilityData.put("available", "Yes");
		jsonAvailabilityData.put("allowGuests", false);
		jsonData.put("availability", jsonAvailabilityData);

		JSONObject jsonContentHandlerData = new JSONObject();
		jsonContentHandlerData.put("id", "resource/x-bb-blti-link");
		//FIXME set the real resource URL
		jsonContentHandlerData.put("url", "http://192.168.1.138:8080/demo/items/" + selectedResource.getUuid() + "/" + selectedResource.getVersion());
		jsonData.put("contentHandler", jsonContentHandlerData);
		LOGGER.info("Attempting to add ["+jsonData.toString()+"] to ["+url+"]");

		final Contents contents = sendBlackboardData(connector, url,
			Contents.class, jsonData, Request.Method.POST);
//		final ConnectorCourse course = new ConnectorCourse(courseId);
//		final List<Content> results = contents.getResults();
//		for( Content content : results )
//		{
//			final Content.ContentHandler handler = content.getContentHandler();
//			if (handler != null && "resource/x-bb-folder".equals(handler.getId()))
//			{
//				// FIXME: filter only user visible folders?
//				final ConnectorFolder cc = new ConnectorFolder(content.getId(), course);
//				// TODO: null safe it
//				cc.setAvailable("Yes".equals(content.getAvailability().getAvailable()));
//				cc.setName(content.getTitle());
//				cc.setLeaf(content.getHasChildren() != null && !content.getHasChildren());
//				//cc.setModifiedDate(content.getCreated());
//				list.add(cc);
//			}
//		}
		return null;
	}

	@Override
	public List<ConnectorContent> findUsages(Connector connector, String username, String uuid, int version, boolean versionIsLatest, boolean archived, boolean allVersion) throws LmsUserNotFoundException
	{
		return null;
	}

	@Override
	public SearchResults<ConnectorContent> findAllUsages(Connector connector, String username, String query, String courseId, String folderId, boolean archived, int offset, int count, ConnectorRepositoryService.ExternalContentSortType sortType, boolean reverseSort) throws LmsUserNotFoundException
	{
		return null;
	}

	@Override
	public int getUnfilteredAllUsagesCount(Connector connector, String username, String query, boolean archived) throws LmsUserNotFoundException
	{
		return 0;
	}

	@Override
	public boolean deleteContent(Connector connector, String username, String contentId) throws LmsUserNotFoundException
	{
		return false;
	}

	@Override
	public boolean editContent(Connector connector, String username, String contentId, String title, String description) throws LmsUserNotFoundException
	{
		return false;
	}

	@Override
	public boolean moveContent(Connector connector, String username, String contentId, String courseId, String folderId) throws LmsUserNotFoundException
	{
		return false;
	}

	@Override
	public ConnectorTerminology getConnectorTerminology()
	{
		ConnectorTerminology terms = new ConnectorTerminology();
		terms.setShowArchived(getKey("finduses.showarchived"));
		terms.setShowArchivedLocations(getKey("finduses.showarchived.courses"));
		terms.setCourseHeading(getKey("finduses.course"));
		terms.setLocationHeading(getKey("finduses.location"));
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
		return false;
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
	public ConnectorCourse getCourse(Connector connector, String courseId)
	{
		return null;
	}

	@Nullable
	private <T> T sendBlackboardData(Connector connector, String path, @Nullable Class<T> returnType,
									  @Nullable Object data, Request.Method method)
	{
		try
		{
			// FIXME; slash check
			final URI uri = URI.create(connector.getServerUrl() + path);

			final Request request = new Request(uri.toString());
			request.setMethod(method);
			request.addHeader("Accept", "application/json");
			if( LOGGER.isTraceEnabled() )
			{
				LOGGER.trace(method + " to Blackboard: " + request.getUrl());
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
			// attach cached token. (Cache knows how to get a new one)
			request.addHeader("Authorization", "Bearer " + getToken(connector.getUuid()));

			//FIXME: Watch for an expired token response.
			//FIXME: If invalid: clear the token cache and then make the request again with the token from the cache
			try( Response response = httpService.getWebContent(request, configService.getProxyDetails()) )
			{
				final String responseBody = response.getBody();
				final int code = response.getCode();
				if( LOGGER.isTraceEnabled() )
				{
					LOGGER.trace("Received from Blackboard (" + code + "):");
					LOGGER.trace(prettyJson(responseBody));
				}
				if( code >= 300 )
				{
					throw new RuntimeException("Received " + code + " from Blackboard. Body = " + responseBody);
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

	private String getToken(String connectorUuid)
	{
		try
		{
			return tokenCache.getCache().get(connectorUuid).get("TOKEN");
		}
		catch (ExecutionException e)
		{
			throw Throwables.propagate(e);
		}
	}

	private String getKey(String partKey)
	{
		return "com.tle.core.connectors.blackboard." + partKey;
	}
}
