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
import com.tle.common.PathUtils;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.searching.SearchResults;
import com.tle.common.util.BlindSSLSocketFactory;
import com.tle.core.connectors.blackboard.BlackboardRESTConnectorConstants;
import com.tle.core.connectors.blackboard.beans.*;
import com.tle.core.connectors.blackboard.service.BlackboardRESTConnectorService;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.AbstractIntegrationConnectorRespository;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.integration.Integration;
import com.tle.web.selection.SelectedResource;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

@NonNullByDefault
@SuppressWarnings({"nls", "deprecation"})
@Bind(BlackboardRESTConnectorService.class)
@Singleton
public class BlackboardRESTConnectorServiceImpl extends AbstractIntegrationConnectorRespository
    implements BlackboardRESTConnectorService {
  private static final Logger LOGGER = Logger.getLogger(BlackboardRESTConnectorService.class);
  private static final String KEY_PFX =
      AbstractPluginService.getMyPluginId(BlackboardRESTConnectorService.class) + ".";

  private static final String API_ROOT = "/learn/api/public/v1";

  @Inject private HttpService httpService;
  @Inject private ConfigurationService configService;
  @Inject private ConnectorService connectorService;
  @Inject private EncryptionService encryptionService;

  private static final String TOKEN_KEY = "TOKEN";
  private InstitutionCache<LoadingCache<String, LoadingCache<String, String>>> tokenCache;

  private static final ObjectMapper jsonMapper = new ObjectMapper();
  private static final ObjectMapper prettyJsonMapper = new ObjectMapper();

  static {
    jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    prettyJsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    prettyJsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    prettyJsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
  }

  public BlackboardRESTConnectorServiceImpl() {
    // Ewwww
    BlindSSLSocketFactory.register();
    // Turn off spurious Pre-emptive Authentication bollocks
    Logger.getLogger("org.apache.commons.httpclient.HttpMethodDirector").setLevel(Level.ERROR);
  }

  @Inject
  public void setInstitutionService(InstitutionService service) {
    tokenCache =
        service.newInstitutionAwareCache(
            new CacheLoader<Institution, LoadingCache<String, LoadingCache<String, String>>>() {
              @Override
              public LoadingCache<String, LoadingCache<String, String>> load(Institution key) {
                // MaximumSize is set to 200, which would allow for 200 Blackboard REST connectors,
                // which should be more than enough for anyone.
                return CacheBuilder.newBuilder()
                    .maximumSize(200)
                    .expireAfterAccess(60, TimeUnit.MINUTES)
                    .build(
                        new CacheLoader<String, LoadingCache<String, String>>() {
                          @Override
                          public LoadingCache<String, String> load(final String connectorUuid)
                              throws Exception {
                            // BB tokens last one hour, so no point holding onto it longer than
                            // that. Of course, we need to handle the case
                            // where we are still holding onto an expired token.

                            return CacheBuilder.newBuilder()
                                .expireAfterWrite(60, TimeUnit.MINUTES)
                                .build(
                                    new CacheLoader<String, String>() {
                                      @Override
                                      public String load(String fixedKey) {
                                        // fixedKey is ignored. It's always TOKEN
                                        final Connector connector =
                                            connectorService.getByUuid(connectorUuid);
                                        final String apiKey =
                                            connector.getAttribute(
                                                BlackboardRESTConnectorConstants.FIELD_API_KEY);
                                        final String apiSecret =
                                            encryptionService.decrypt(
                                                connector.getAttribute(
                                                    BlackboardRESTConnectorConstants
                                                        .FIELD_API_SECRET));
                                        final String b64 =
                                            new Base64()
                                                .encode((apiKey + ":" + apiSecret).getBytes())
                                                .replace("\n", "")
                                                .replace("\r", "");

                                        final Request req =
                                            new Request(
                                                PathUtils.urlPath(
                                                    connector.getServerUrl(),
                                                    "learn/api/public/v1/oauth2/token"));
                                        req.setMethod(Request.Method.POST);
                                        req.setMimeType("application/x-www-form-urlencoded");
                                        req.addHeader("Authorization", "Basic " + b64);
                                        req.setBody("grant_type=client_credentials");
                                        try (final Response resp =
                                            httpService.getWebContent(
                                                req, configService.getProxyDetails())) {
                                          final Token token =
                                              jsonMapper.readValue(
                                                  resp.getInputStream(), Token.class);
                                          return token.getAccessToken();
                                        } catch (Exception e) {
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
  protected ViewableItemType getViewableItemType() {
    return ViewableItemType.GENERIC;
  }

  @Override
  protected String getIntegrationId() {
    return "gen";
  }

  @Override
  protected boolean isRelativeUrls() {
    return false;
  }

  @Override
  public boolean isRequiresAuthentication(Connector connector) {
    return false;
  }

  @Override
  public String getAuthorisationUrl(Connector connector, String forwardUrl, String authData) {
    return null;
  }

  @Override
  public String getCourseCode(Connector connector, String username, String courseId)
      throws LmsUserNotFoundException {
    return null;
  }

  @Override
  public List<ConnectorCourse> getCourses(
      Connector connector,
      String username,
      boolean editableOnly,
      boolean archived,
      boolean management)
      throws LmsUserNotFoundException {
    final List<ConnectorCourse> list = new ArrayList<>();

    // FIXME: courses for current user...?
    // TODO - since v3400.8.0, this endpoint should use v2
    String url = API_ROOT + "/courses";
    /*
    if( !archived )
    {
    	url += "&active=true";
    }*/
    final List<Course> allCourses = new ArrayList<>();

    // TODO: a more generic way of doing paged results. Contents also does paging
    Courses courses = sendBlackboardData(connector, url, Courses.class, null, Request.Method.GET);
    allCourses.addAll(courses.getResults());
    Paging paging = courses.getPaging();

    while (paging != null && paging.getNextPage() != null) {
      // FIXME: construct nextUrl from the base URL we know about and the relative URL from
      // getNextPage
      final String nextUrl = paging.getNextPage();
      courses = sendBlackboardData(connector, nextUrl, Courses.class, null, Request.Method.GET);
      allCourses.addAll(courses.getResults());
      paging = courses.getPaging();
    }

    for (Course course : allCourses) {
      // Display all courses if the archived flag is set, otherwise, just the 'available' ones
      if (archived || Availability.YES.equals(course.getAvailability().getAvailable())) {
        final ConnectorCourse cc = new ConnectorCourse(course.getId());
        cc.setCourseCode(course.getCourseId());
        cc.setName(course.getName());
        cc.setAvailable(true);
        list.add(cc);
      }
    }

    return list;
  }

  private Course getCourseBean(Connector connector, String courseID) {
    // FIXME: courses for current user...?
    // TODO - since v3400.8.0, this endpoint should use v2
    String url = API_ROOT + "/courses/" + courseID;

    final Course course =
        sendBlackboardData(connector, url, Course.class, null, Request.Method.GET);
    return course;
  }

  private Content getContentBean(Connector connector, String courseID, String folderID) {
    // FIXME: courses for current user...?
    // TODO - since v3400.8.0, this endpoint should use v2
    String url = API_ROOT + "/courses/" + courseID + "/contents/" + folderID;

    final Content folder =
        sendBlackboardData(connector, url, Content.class, null, Request.Method.GET);
    return folder;
  }

  @Override
  public List<ConnectorFolder> getFoldersForCourse(
      Connector connector, String username, String courseId, boolean management)
      throws LmsUserNotFoundException {
    // FIXME: courses for current user...?
    final String url = API_ROOT + "/courses/" + courseId + "/contents";

    return retrieveFolders(connector, url, username, courseId, management);
  }

  @Override
  public List<ConnectorFolder> getFoldersForFolder(
      Connector connector, String username, String courseId, String folderId, boolean management)
      throws LmsUserNotFoundException {
    // FIXME: courses for current user...?
    final String url = API_ROOT + "/courses/" + courseId + "/contents/" + folderId + "/children/";

    return retrieveFolders(connector, url, username, courseId, management);
  }

  private List<ConnectorFolder> retrieveFolders(
      Connector connector, String url, String username, String courseId, boolean management) {
    final List<ConnectorFolder> list = new ArrayList<>();

    final Contents contents =
        sendBlackboardData(connector, url, Contents.class, null, Request.Method.GET);
    final ConnectorCourse course = new ConnectorCourse(courseId);
    final List<Content> results = contents.getResults();
    for (Content content : results) {
      final Content.ContentHandler handler = content.getContentHandler();
      if (handler != null && Content.ContentHandler.RESOURCE_FOLDER.equals(handler.getId())) {
        // Unavailable folders are inaccessible to students,
        // but should be available for instructors to push content to.
        final ConnectorFolder cc = new ConnectorFolder(content.getId(), course);
        if (content.getAvailability() != null) {
          cc.setAvailable(Availability.YES.equals(content.getAvailability().getAvailable()));
        } else {
          // FIXME:  Is this an appropriate default?
          cc.setAvailable(false);
        }
        cc.setName(content.getTitle());
        cc.setLeaf(content.getHasChildren() != null && !content.getHasChildren());
        list.add(cc);
      }
    }

    return list;
  }

  @Override
  public ConnectorFolder addItemToCourse(
      Connector connector,
      String username,
      String courseId,
      String folderId,
      IItem<?> item,
      SelectedResource selectedResource)
      throws LmsUserNotFoundException {
    final String url = API_ROOT + "/courses/" + courseId + "/contents/" + folderId + "/children";

    final Integration.LmsLinkInfo linkInfo = getLmsLink(item, selectedResource);
    final Integration.LmsLink lmsLink = linkInfo.getLmsLink();

    final Content content = new Content();
    content.setTitle(lmsLink.getName());
    // TODO consider a nicer way to handle this.  Bb needs the description to be 250 chars or less
    // Using TextUtils.INSTANCE.ensureWrap(lmsLink.getDescription(),250, 250, true) doesn't work
    // because it still can produce a **raw** string longer than 250 characters.  Bb content link
    // descriptions can handle html formatting.  Doesn't look there is a configuration to
    // change the ensureWrap behavior - I'm reverting this so long descriptions won't block the
    // integration.
    final String lmsLinkDesc = lmsLink.getDescription();
    content.setDescription(
        lmsLinkDesc.substring(0, (lmsLinkDesc.length() > 250) ? 250 : lmsLinkDesc.length()));

    final Content.ContentHandler contentHandler = new Content.ContentHandler();
    contentHandler.setId(Content.ContentHandler.RESOURCE_LTI_LINK);
    contentHandler.setUrl(lmsLink.getUrl());
    content.setContentHandler(contentHandler);

    final Availability availability = new Availability();
    availability.setAvailable(Availability.YES);
    availability.setAllowGuests(true);
    content.setAvailability(availability);

    sendBlackboardData(connector, url, null, content, Request.Method.POST);
    LOGGER.trace("Returning a courseId = [" + courseId + "],  and folderId = [" + folderId + "]");
    ConnectorFolder cf = new ConnectorFolder(folderId, new ConnectorCourse(courseId));
    // CB:  Is there a better way to get the name of the folder and the course?
    // AH:  Unfortunately not.  We could cache them, but it probably isn't worth the additional
    // complexity
    Content folder = getContentBean(connector, courseId, folderId);
    cf.setName(folder.getTitle());
    Course course = getCourseBean(connector, courseId);
    cf.getCourse().setName(course.getName());
    return cf;
  }

  @Override
  public List<ConnectorContent> findUsages(
      Connector connector,
      String username,
      String uuid,
      int version,
      boolean versionIsLatest,
      boolean archived,
      boolean allVersion)
      throws LmsUserNotFoundException {
    return null;
  }

  @Override
  public SearchResults<ConnectorContent> findAllUsages(
      Connector connector,
      String username,
      String query,
      String courseId,
      String folderId,
      boolean archived,
      int offset,
      int count,
      ConnectorRepositoryService.ExternalContentSortType sortType,
      boolean reverseSort)
      throws LmsUserNotFoundException {
    return null;
  }

  @Override
  public int getUnfilteredAllUsagesCount(
      Connector connector, String username, String query, boolean archived)
      throws LmsUserNotFoundException {
    return 0;
  }

  @Override
  public boolean deleteContent(Connector connector, String username, String contentId)
      throws LmsUserNotFoundException {
    return false;
  }

  @Override
  public boolean editContent(
      Connector connector, String username, String contentId, String title, String description)
      throws LmsUserNotFoundException {
    return false;
  }

  @Override
  public boolean moveContent(
      Connector connector, String username, String contentId, String courseId, String folderId)
      throws LmsUserNotFoundException {
    return false;
  }

  @Override
  public ConnectorTerminology getConnectorTerminology() {
    final ConnectorTerminology terms = new ConnectorTerminology();
    terms.setShowArchived(getKey("finduses.showarchived"));
    terms.setShowArchivedLocations(getKey("finduses.showarchived.courses"));
    terms.setCourseHeading(getKey("finduses.course"));
    terms.setLocationHeading(getKey("finduses.location"));
    return terms;
  }

  @Override
  public boolean supportsExport() {
    return true;
  }

  @Override
  public boolean supportsEdit() {
    return true;
  }

  @Override
  public boolean supportsView() {
    return true;
  }

  @Override
  public boolean supportsDelete() {
    return true;
  }

  @Override
  public boolean supportsCourses() {
    return false;
  }

  @Override
  public boolean supportsFindUses() {
    return false;
  }

  @Override
  public boolean supportsReverseSort() {
    return false;
  }

  @Nullable
  private <T> T sendBlackboardData(
      Connector connector,
      String path,
      @Nullable Class<T> returnType,
      @Nullable Object data,
      Request.Method method) {
    return sendBlackboardData(connector, path, returnType, data, method, true);
  }

  @Nullable
  private <T> T sendBlackboardData(
      Connector connector,
      String path,
      @Nullable Class<T> returnType,
      @Nullable Object data,
      Request.Method method,
      boolean firstTime) {
    try {
      final URI uri = URI.create(PathUtils.urlPath(connector.getServerUrl(), path));

      final Request request = new Request(uri.toString());
      request.setMethod(method);
      request.addHeader("Accept", "application/json");
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(method + " to Blackboard: " + request.getUrl());
      }

      final String body;
      if (data != null) {
        body = jsonMapper.writeValueAsString(data);
      } else {
        body = "";
      }
      request.setBody(body);
      if (body.length() > 0) {
        request.addHeader("Content-Type", "application/json");
      }
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Sending " + prettyJson(body));
      }
      // attach cached token. (Cache knows how to get a new one)
      request.addHeader("Authorization", "Bearer " + getToken(connector.getUuid()));

      try (Response response =
          httpService.getWebContent(request, configService.getProxyDetails())) {
        final String responseBody = response.getBody();
        final int code = response.getCode();
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Received from Blackboard (" + code + "):");
          LOGGER.trace(prettyJson(responseBody));
        }
        if (code == 401 && firstTime) {
          // Unauthorized request.  Retry once to obtain a new token (assumes the current token is
          // expired)
          LOGGER.trace(
              "Received a 401 from Blackboard.  Token for connector ["
                  + connector.getUuid()
                  + "] is likely expired.  Retrying...");
          tokenCache.getCache().get(connector.getUuid()).invalidate(TOKEN_KEY);
          return sendBlackboardData(connector, path, returnType, data, method, false);
        }
        if (code >= 300) {
          throw new RuntimeException(
              "Received " + code + " from Blackboard. Body = " + responseBody);
        }
        if (returnType != null) {
          final T content = jsonMapper.readValue(responseBody, returnType);
          return content;
        }
        return null;
      }
    } catch (ExecutionException | IOException ex) {
      throw Throwables.propagate(ex);
    }
  }

  @Nullable
  private String prettyJson(@Nullable String json) {
    if (Strings.isNullOrEmpty(json)) {
      return json;
    }
    try {
      return prettyJsonMapper.writeValueAsString(prettyJsonMapper.readTree(json));
    } catch (IOException io) {
      return json;
    }
  }

  private String getToken(String connectorUuid) {
    try {
      return tokenCache.getCache().get(connectorUuid).get(TOKEN_KEY);
    } catch (ExecutionException e) {
      throw Throwables.propagate(e);
    }
  }

  private String getKey(String partKey) {
    return KEY_PFX + "blackboardrest." + partKey;
  }
}
