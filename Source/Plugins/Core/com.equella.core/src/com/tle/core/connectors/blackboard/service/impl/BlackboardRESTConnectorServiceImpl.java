/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ViewableItemType;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.searching.SearchResults;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.util.BlindSSLSocketFactory;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.connectors.blackboard.BlackboardRESTConnectorConstants;
import com.tle.core.connectors.blackboard.BlackboardRestAppContext;
import com.tle.core.connectors.blackboard.beans.*;
import com.tle.core.connectors.blackboard.service.BlackboardRESTConnectorService;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.AbstractIntegrationConnectorRespository;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.connectors.utils.ConnectorEntityUtils;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.replicatedcache.ReplicatedCacheService;
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.AuthenticationException;
import com.tle.web.integration.Integration;
import com.tle.web.selection.SelectedResource;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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

  private static final String API_ROOT_V1 = "/learn/api/public/v1/";
  private static final String API_ROOT_V3 = "/learn/api/public/v3/";

  // Used to encrypt and decrypt state information (such as connector uuid)
  // during the integration flows. Actual values are not important.
  // TODO expose as a user configuration.
  private static final byte[] SHAREPASS =
      new byte[] {45, 12, -112, 2, 89, 97, 19, 74, 0, 24, -118, -2, 5, 108, 92, 7};
  private static final IvParameterSpec INITVEC = new IvParameterSpec("thisis16byteslog".getBytes());

  @Inject private HttpService httpService;
  @Inject private ConfigurationService configService;
  @Inject private ConnectorService connectorService;
  @Inject private ReplicatedCacheService cacheService;
  @Inject private InstitutionService institutionService;
  @Inject private AuditLogService auditService;
  @Inject private UserSessionService userSessionService;
  @Inject private EncryptionService encryptionService;

  private static final String CACHE_ID_COURSE = "BbRestCoursesByUser";
  private static final String CACHE_ID_COURSE_FOLDERS = "BbRestFoldersByUserAndCourse";
  private static final String CACHE_ID_AUTH = "BbRestAuthByUser";

  private ReplicatedCache<ImmutableList<Course>> courseCache;
  private ReplicatedCache<ImmutableList<ConnectorFolder>> courseFoldersCache;
  private ReplicatedCache<Token> authCache;

  private static final ObjectMapper jsonMapper = new ObjectMapper();
  private static final ObjectMapper prettyJsonMapper = new ObjectMapper();

  static {
    jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    prettyJsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    prettyJsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    prettyJsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
  }

  @PostConstruct
  public void setupCache() {
    // These two caches can be easily rebuilt, and the original
    //  data on the Bb servers can frequently change
    courseCache = cacheService.getCache(CACHE_ID_COURSE, 100, 2, TimeUnit.MINUTES);
    courseFoldersCache = cacheService.getCache(CACHE_ID_COURSE_FOLDERS, 100, 2, TimeUnit.MINUTES);
    // Bb tokens expire after 60 minutes.  We also leverage the refresh_token flow, since
    //  refreshing a token is less noticeable to the user then requesting a new access token
    authCache = cacheService.getCache(CACHE_ID_AUTH, 1000, 60, TimeUnit.MINUTES);
  }

  public BlackboardRESTConnectorServiceImpl() {
    // Ewwww
    BlindSSLSocketFactory.register();
    // Turn off spurious Pre-emptive Authentication bollocks
    Logger.getLogger("org.apache.commons.httpclient.HttpMethodDirector").setLevel(Level.ERROR);
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

  /**
   * See {@link
   * com.tle.core.connectors.service.ConnectorRepositoryImplementation#isRequiresAuthentication(Connector)}
   * Also loads the auth Token into the user session
   *
   * @param connector external system connector for authorization details
   * @return if an auth dialog needs to be displayed to continue
   */
  @Override
  public boolean isRequiresAuthentication(Connector connector) {
    Optional<Token> auth = getAuth(connector);
    if (auth.isPresent()) {
      LOGGER.debug("User has the needed cached details, no authorization needed");
      setUserSessionAuth(auth.get());
      return false;
    }

    LOGGER.debug("User does not have active cached details and will need to authorize");
    return true;
  }

  @Override
  public String getAuthorisationUrl(
      Connector connector, String forwardUrl, @Nullable String authData) {
    final BlackboardRestAppContext appContext = getAppContext(connector);
    return getAuthorisationUrl(appContext, forwardUrl, authData, connector.getUuid());
  }

  @Override
  public String getAuthorisationUrl(
      String appId,
      String appKey,
      String bbServerUrl,
      String forwardUrl,
      @Nullable String postfixKey) {
    final BlackboardRestAppContext appContext = getAppContext(appId, appKey, bbServerUrl);
    return getAuthorisationUrl(appContext, forwardUrl, postfixKey, null);
  }

  private String getAuthorisationUrl(
      BlackboardRestAppContext appContext,
      String forwardUrl,
      @Nullable String postfixKey,
      String connectorUuid) {
    LOGGER.trace("Requesting auth url for [" + connectorUuid + "]");
    final ObjectMapper mapper = new ObjectMapper();
    final ObjectNode stateJson = mapper.createObjectNode();
    final String fUrl =
        institutionService.getInstitutionUrl()
            + "/api/connector/"
            + stateJson.put(BlackboardRESTConnectorConstants.STATE_KEY_FORWARD_URL, forwardUrl);
    if (postfixKey != null) {
      stateJson.put(BlackboardRESTConnectorConstants.STATE_KEY_POSTFIX_KEY, postfixKey);
    }
    stateJson.put("connectorUuid", connectorUuid);
    URI uri;
    try {
      uri =
          appContext.createWebUrlForAuthentication(
              URI.create(
                  institutionService.institutionalise(BlackboardRESTConnectorConstants.AUTH_URL)),
              encrypt(mapper.writeValueAsString(stateJson)));
    } catch (JsonProcessingException e) {
      LOGGER.trace("Unable to provide the auth url for [" + connectorUuid + "]");
      throw Throwables.propagate(e);
    }
    return uri.toString();
  }

  @Override
  public String getCourseCode(Connector connector, String username, String courseId)
      throws LmsUserNotFoundException {
    return null;
  }

  /**
   * Requests courses the user has access to from Blackboard and caches them (per user & connector)
   *
   * @param connector
   * @param username
   * @param editableOnly If true as list of courses that the user can add content to should be
   *     returned. If false then ALL courses will be returned.
   * @param archived
   * @param management Is this for manage resources?
   * @return
   */
  @Override
  public List<ConnectorCourse> getCourses(
      Connector connector,
      String username,
      boolean editableOnly,
      boolean archived,
      boolean management) {
    final List<ConnectorCourse> list = new ArrayList<>();
    final ImmutableList<Course> allCourses = getCachedCourses(connector);

    return allCourses.stream()
        // Display all courses if the archived flag is set, otherwise, just the 'available' ones
        .filter(
            course -> archived || Availability.YES.equals(course.getAvailability().getAvailable()))
        // Convert Course to ConnectorCourse
        .map(
            course -> {
              final ConnectorCourse cc = new ConnectorCourse(course.getId());
              cc.setCourseCode(course.getCourseId());
              cc.setName(course.getName());
              cc.setAvailable(true);
              return cc;
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<ConnectorFolder> getFoldersForCourse(
      Connector connector, String username, String courseId, boolean management)
      throws LmsUserNotFoundException {

    return retrieveFolders(connector, courseId, management);
  }

  @Override
  public List<ConnectorFolder> getFoldersForFolder(
      Connector connector, String username, String courseId, String folderId, boolean management) {
    // Username not needed to since we authenticate via 3LO.

    List<ConnectorFolder> folders = retrieveFolders(connector, courseId, management);
    for (ConnectorFolder folder : folders) {
      Optional<ConnectorFolder> foundFolder = ConnectorEntityUtils.findFolder(folder, folderId);
      if (foundFolder.isPresent()) {
        return foundFolder.get().getFolders();
      }
    }

    // Cache of folders exist, but folderId was not found
    // Ideally return Optional, but this requires a broader refactor.
    return null;
  }

  private List<ConnectorFolder> retrieveFolders(
      Connector connector, String courseId, boolean management) {

    final Optional<ImmutableList<ConnectorFolder>> cachedFolders =
        getCachedCourseFolders(connector, courseId);

    if (cachedFolders.isPresent()) {
      return cachedFolders.get();
    }

    // No cached folders available.  Pull latest from Blackboard.
    Optional<String> url =
        Optional.of(API_ROOT_V1 + "courses/" + courseId + "/contents?recursive=true");
    final List<Content> allContent = new ArrayList<>();
    do {
      Contents contents =
          sendBlackboardData(connector, url.get(), Contents.class, null, Request.Method.GET);
      allContent.addAll(contents.getResults());
      url = Optional.ofNullable(contents.getPaging()).map(Paging::getNextPage);
    } while (url.isPresent());

    final ConnectorCourse course = new ConnectorCourse(courseId);
    final ImmutableList<ConnectorFolder> folders =
        ImmutableList.copyOf(ConnectorEntityUtils.parseFolders(allContent, course));
    setCachedCourseFolders(connector, courseId, folders);
    return folders;
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
    final String url = API_ROOT_V1 + "courses/" + courseId + "/contents/" + folderId + "/children";

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
    LOGGER.debug("Returning a courseId = [" + courseId + "],  and folderId = [" + folderId + "]");
    ConnectorFolder cf = new ConnectorFolder(folderId, new ConnectorCourse(courseId));

    final Optional<ConnectorFolder> cachedFolder =
        getCachedCourseFolder(connector, courseId, folderId);
    if (cachedFolder.isPresent()) {
      cf.setName(cachedFolder.get().getName());
    }

    final Optional<Course> cachedCourse = getCachedCourse(connector, courseId);
    // Not a big deal if not present, likely coming from an LTI Launch
    if (cachedCourse.isPresent()) {
      cf.getCourse().setName(cachedCourse.get().getName());
    }

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
    LOGGER.debug("Requesting Bb REST connector terminology");
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
      final Request request = prepareBlackboardCall(connector, path, data, method);

      try (Response response =
          httpService.getWebContent(request, configService.getProxyDetails())) {
        final String responseBody = response.getBody();
        captureBlackboardRateLimitMetrics(request.getUrl(), response);
        final int code = response.getCode();
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Received from Blackboard (" + code + "):" + prettyJson(responseBody));
        }
        if (code == 401 && firstTime) {
          // Unauthorized request.  Retry once to obtain a new token (assumes the current token is
          // expired)
          LOGGER.debug(
              "Received a 401 from Blackboard.  Token for connector ["
                  + connector.getUuid()
                  + "] is likely expired.  Trying to refresh...");
          refreshBlackboardToken(connector);

          // Retry original call
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
    } catch (IOException ex) {
      throw Throwables.propagate(ex);
    }
  }

  private <T> Request prepareBlackboardCall(
      Connector connector, String path, @Nullable Object data, Request.Method method)
      throws JsonProcessingException {
    final URI uri = URI.create(PathUtils.urlPath(connector.getServerUrl(), path));

    final Request request = new Request(uri.toString());
    request.setMethod(method);
    request.addHeader("Accept", "application/json");
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(method + " to Blackboard: " + request.getUrl());
    }

    // Setup request body
    request.setBody((data != null) ? jsonMapper.writeValueAsString(data) : "");
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Sending " + prettyJson(request.getBody()));
    }

    if ("grant_type=refresh_token".equals(data)) {
      request.setMimeType("application/x-www-form-urlencoded");
      request.addHeader("Authorization", "Basic " + buildBasicAuthorizationCredentials(connector));
    } else {
      // Normal request flow
      if (request.getBody().length() > 0) {
        request.addHeader("Content-Type", "application/json");
      }
      // attach cached token
      final String authHeaderValue = "Bearer " + getUserSessionAuth().getAccessToken();
      request.addHeader("Authorization", authHeaderValue);
    }

    return request;
  }

  private Token getUserSessionAuth() {
    final Optional<Token> toRet =
        Optional.ofNullable(
            userSessionService.getAttribute(
                BlackboardRESTConnectorConstants.USER_SESSION_AUTH_KEY));
    return toRet.orElseThrow(
        () ->
            new AuthenticationException(
                "User authentication details are not available in the session"));
  }

  private void setUserSessionAuth(Token t) {
    userSessionService.setAttribute(BlackboardRESTConnectorConstants.USER_SESSION_AUTH_KEY, t);
  }

  private void refreshBlackboardToken(Connector connector) {
    // Make a reasonable effort to obtain the latest refresh token
    Token expAuth = getAuth(connector).orElse(getUserSessionAuth());

    final String path =
        "learn/api/public/v1/oauth2/token?grant_type=refresh_token&refresh_token="
            + expAuth.getRefreshToken();

    try {
      // Setting 'firstTime' to false - if the token refresh fails, it's likely not recoverable.
      setAuth(
          connector,
          sendBlackboardData(
              connector,
              path,
              Token.class,
              "grant_type=refresh_token",
              Request.Method.POST,
              false));
    } catch (Exception exception) {
      LOGGER.error(exception.getMessage());
      // Likely an expired auth token that failed to refresh.  Guide the user to try again
      removeCachedValue(authCache, buildCacheKey(CACHE_ID_AUTH, connector));
      throw new AuthenticationException("Unable to refresh auth token.  Please retry action.");
    }
  }

  private void captureBlackboardRateLimitMetrics(String url, Response response) {
    final String xrlLimit = response.getHeader("X-Rate-Limit-Limit");
    final String xrlRemaining = response.getHeader("X-Rate-Limit-Remaining");
    final String xrlReset = response.getHeader("X-Rate-Limit-Reset");
    LOGGER.debug("X-Rate-Limit-Limit = [" + xrlLimit + "]");
    LOGGER.debug("X-Rate-Limit-Remaining = [" + xrlRemaining + "]");
    LOGGER.debug("X-Rate-Limit-Reset = [" + xrlReset + "]");
    auditService.logExternalConnectorUsed(url, xrlLimit, xrlRemaining, xrlReset);
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

  private String getKey(String partKey) {
    return KEY_PFX + "blackboardrest." + partKey;
  }

  private BlackboardRestAppContext getAppContext(Connector connector) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
          "Blackboard REST connector attributes: "
              + Arrays.toString(connector.getAttributes().keySet().toArray()));
    }
    return getAppContext(
        connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_API_KEY),
        connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_API_SECRET),
        connector.getServerUrl());
  }

  private BlackboardRestAppContext getAppContext(String appId, String appKey, String serverUrl) {
    return new BlackboardRestAppContext(appId, appKey, serverUrl);
  }

  @Override
  public String encrypt(String data) {
    LOGGER.debug("Encrypting data");
    if (!Check.isEmpty(data)) {
      try {
        SecretKey key = new SecretKeySpec(SHAREPASS, "AES");
        Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ecipher.init(Cipher.ENCRYPT_MODE, key, INITVEC);

        // Encrypt
        byte[] enc = ecipher.doFinal(data.getBytes());
        return new Base64().encode(enc);

      } catch (Exception e) {
        throw new RuntimeException("Error encrypting", e);
      }
    }

    return data;
  }

  @Override
  public String decrypt(String encryptedData) {
    LOGGER.debug("Decrypting data");
    if (!Check.isEmpty(encryptedData)) {
      try {
        byte[] bytes = new Base64().decode(encryptedData);
        SecretKey key = new SecretKeySpec(SHAREPASS, "AES");
        Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ecipher.init(Cipher.DECRYPT_MODE, key, INITVEC);
        return new String(ecipher.doFinal(bytes));
      } catch (Exception e) {
        throw new RuntimeException("Error decrypting ", e);
      }
    }

    return encryptedData;
  }

  @Override
  public String buildBasicAuthorizationCredentials(Connector connector) {
    final String apiKey = connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_API_KEY);
    final String apiSecret =
        encryptionService.decrypt(
            connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_API_SECRET));
    return new Base64()
        .encode((apiKey + ":" + apiSecret).getBytes())
        .replace("\n", "")
        .replace("\r", "");
  }

  public Optional<Token> getAuth(Connector connector) {
    return getCachedValue(authCache, buildCacheKey(CACHE_ID_AUTH, connector));
  }

  private void removeCachedValuesForConnector(Connector connector) {
    removeCachedCoursesForConnector(connector);
    removeCachedValue(authCache, buildCacheKey(CACHE_ID_AUTH, connector));
  }

  public void removeCachedCoursesForConnector(Connector connector) {
    final Optional<ImmutableList<Course>> cached =
        getCachedValue(courseCache, buildCacheKey(CACHE_ID_COURSE, connector));
    if (cached.isPresent()) {
      removeCachedValue(courseCache, buildCacheKey(CACHE_ID_COURSE, connector));
      cached.get().stream()
          .forEach(
              c ->
                  removeCachedValue(
                      courseFoldersCache,
                      buildCacheKey(CACHE_ID_COURSE_FOLDERS, c.getId(), connector)));
    }
  }

  private String getUserIdType() {
    // According to the Bb Support team, accessing the REST APIs in this manner should always return
    // a userid as a uuid
    return "uuid:";
  }

  /**
   * Sets both caches (replicated and user session) to the token
   *
   * @param connector
   * @param token
   */
  public void setAuth(Connector connector, Token token) {
    setCachedValue(authCache, buildCacheKey(CACHE_ID_AUTH, connector), token);
    setUserSessionAuth(token);
  }

  private void setCachedCourses(Connector connector, ImmutableList<Course> courses) {
    final String key = buildCacheKey(CACHE_ID_COURSE, connector);
    LOGGER.debug("Setting cache " + key + " - number of cached courses [" + courses.size() + "]");

    setCachedValue(courseCache, key, courses);
  }

  /**
   * First tries to return the cached courses. If none are available, request them from Blackboard,
   * cache them, and return the now cached courses.
   *
   * @param connector
   * @return
   */
  private ImmutableList<Course> getCachedCourses(Connector connector) {
    final String key = buildCacheKey(CACHE_ID_COURSE, connector);
    final Optional<ImmutableList<Course>> cached = getCachedValue(courseCache, key);
    if (cached.isPresent()) {
      return cached.get();
    }

    // Not cached - get a fresh copy
    Optional<String> url =
        Optional.of(
            API_ROOT_V1
                + "users/"
                + getUserIdType()
                + getUserSessionAuth().getUserId()
                + "/courses?fields=course");

    final List<Course> allCourses = new ArrayList<>();
    do {
      CoursesByUser courses =
          sendBlackboardData(connector, url.get(), CoursesByUser.class, null, Request.Method.GET);
      allCourses.addAll(
          courses.getResults().stream().map(CourseByUser::getCourse).collect(Collectors.toList()));
      url = Optional.ofNullable(courses.getPaging()).map(Paging::getNextPage);
    } while (url.isPresent());

    final ImmutableList<Course> lockedList = ImmutableList.copyOf(allCourses);
    setCachedCourses(connector, lockedList);
    return lockedList;
  }

  private void setCachedCourseFolders(
      Connector connector, String courseId, ImmutableList<ConnectorFolder> folders) {
    final String key = buildCacheKey(CACHE_ID_COURSE_FOLDERS, courseId, connector);
    LOGGER.debug("Setting cache " + key + " - number of cached folders [" + folders.size() + "]");
    setCachedValue(courseFoldersCache, key, folders);
  }

  private Optional<ImmutableList<ConnectorFolder>> getCachedCourseFolders(
      Connector connector, String courseId) {
    final String key = buildCacheKey(CACHE_ID_COURSE_FOLDERS, courseId, connector);
    return getCachedValue(courseFoldersCache, key);
  }

  private Optional<ConnectorFolder> getCachedCourseFolder(
      Connector connector, String courseId, String folderId) {
    final String key = buildCacheKey(CACHE_ID_COURSE_FOLDERS, courseId, connector);

    final Optional<ImmutableList<ConnectorFolder>> folders =
        getCachedValue(courseFoldersCache, key);

    if (!folders.isPresent()) {
      return Optional.empty();
    }

    for (ConnectorFolder topLevelFolder : folders.get()) {
      // Interesting bit here - we are looping through the top level folders looking
      //  for a folder - the `foundFolder` _may_ be the `folder`, or it _may_ be a
      //  descendant of the `folder`.
      // Due to findFolder possibly returning a descendant of `folder`, it's not
      //  suited well for the `streams` api.
      Optional<ConnectorFolder> possibleFoundFolder =
          ConnectorEntityUtils.findFolder(topLevelFolder, folderId);
      if (possibleFoundFolder.isPresent()) {
        return possibleFoundFolder;
      }
    }

    return Optional.empty();
  }

  private Optional<Course> getCachedCourse(Connector connector, String courseId) {
    return getCachedCourses(connector).stream().filter(c -> c.getId().equals(courseId)).findFirst();
  }

  /**
   * This unfortunate situation is due to a mixed usage of Google Optional and java.util.Optional.
   *
   * @param obj
   * @param <T>
   * @return
   */
  private <T> Optional<T> convert(com.google.common.base.Optional<T> obj) {
    if (obj.isPresent()) {
      return Optional.of(obj.get());
    } else {
      return Optional.empty();
    }
  }

  private String buildCacheKey(String cacheId, String courseId, Connector connector) {
    return cacheId
        + "-C:"
        + connector.getUuid()
        + "-U:"
        + CurrentUser.getUserID()
        + "-CID:"
        + courseId;
  }

  private String buildCacheKey(String cacheId, Connector connector) {
    return cacheId + "-C:" + connector.getUuid() + "-U:" + CurrentUser.getUserID();
  }

  private <T extends Serializable> Optional<T> getCachedValue(
      ReplicatedCache<T> cache, String key) {
    final Optional<T> cachedValue = convert(cache.get(key));
    if (LOGGER.isDebugEnabled()) {
      if (!cachedValue.isPresent()) {
        LOGGER.debug("No cache available for " + key);
      } else {
        logSensitiveDetails("Found a cached value for " + key, " - value [" + cachedValue + "]");
      }
    }
    return cachedValue;
  }

  private <T extends Serializable> void setCachedValue(
      ReplicatedCache<T> cache, String key, T value) {
    logSensitiveDetails("Setting cache " + key, " to [" + value + "]");
    cache.put(key, value);
  }

  private <T extends Serializable> void removeCachedValue(ReplicatedCache<T> cache, String key) {
    LOGGER.debug("Invalidating cache " + key);
    cache.invalidate(key);
  }

  private void logSensitiveDetails(String msg, String sensitiveMsg) {
    if (LOGGER.isTraceEnabled()) {
      // NOTE:  Use with care - exposes sensitive details.  Only to be used for investigations
      LOGGER.trace(msg + sensitiveMsg);
    } else {
      LOGGER.debug(msg);
    }
  }
}
