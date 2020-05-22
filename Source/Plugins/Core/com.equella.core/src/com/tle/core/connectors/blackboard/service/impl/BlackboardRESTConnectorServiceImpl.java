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
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.AuthenticationException;
import com.tle.web.integration.Integration;
import com.tle.web.selection.SelectedResource;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

  private static final byte[] SHAREPASS =
      new byte[] {45, 123, -112, 2, 89, 124, 19, 74, 0, 24, -118, 98, 5, 100, 92, 7};
  private static final IvParameterSpec INITVEC = new IvParameterSpec("thisis16byteslog".getBytes());

  @Inject private HttpService httpService;
  @Inject private ConfigurationService configService;
  @Inject private ConnectorService connectorService;
  @Inject private UserSessionService userSessionService;
  @Inject private InstitutionService institutionService;
  @Inject private AuditLogService auditService;

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
    try {
      getToken(connector);
      getUserId(connector);
		LOGGER.debug("User session doesn't require auth for connector [" + connector.getUuid() + "]");
      return false;
    } catch (AuthenticationException ex) {
		LOGGER.debug("User session requires auth for connector [" + connector.getUuid() + "]");
      return true;
    }
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
    final String fUrl = institutionService.getInstitutionUrl() + "/api/connector/" +
    stateJson.put(BlackboardRESTConnectorConstants.STATE_KEY_FORWARD_URL, forwardUrl);
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

  @Override
  public List<ConnectorCourse> getCourses(
      Connector connector,
      String username,
      boolean editableOnly,
      boolean archived,
      boolean management) {
    final List<ConnectorCourse> list = new ArrayList<>();

    String url = API_ROOT_V1 + "users/" + getUserIdType() + getUserId(connector) + "/courses?fields=course";

    final List<Course> allCourses = new ArrayList<>();

    // TODO (post new UI): a more generic way of doing paged results. Contents also does paging
    CoursesByUser courses = sendBlackboardData(connector, url, CoursesByUser.class, null, Request.Method.GET);
    for(CourseByUser cbu : courses.getResults()) {
		allCourses.add(cbu.getCourse());
	}
    Paging paging = courses.getPaging();

    while (paging != null && paging.getNextPage() != null) {
      // FIXME: construct nextUrl from the base URL we know about and the relative URL from
      // getNextPage
      final String nextUrl = paging.getNextPage();
      courses = sendBlackboardData(connector, nextUrl, CoursesByUser.class, null, Request.Method.GET);
		for(CourseByUser cbu : courses.getResults()) {
			allCourses.add(cbu.getCourse());
		}paging = courses.getPaging();
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
    String url = API_ROOT_V1 + "courses/" + courseID;

    final Course course =
        sendBlackboardData(connector, url, Course.class, null, Request.Method.GET);
    return course;
  }

  private Content getContentBean(Connector connector, String courseID, String folderID) {
    // FIXME: courses for current user...?
    // TODO - since v3400.8.0, this endpoint should use v2
    String url = API_ROOT_V1 + "courses/" + courseID + "/contents/" + folderID;

    final Content folder =
        sendBlackboardData(connector, url, Content.class, null, Request.Method.GET);
    return folder;
  }

  @Override
  public List<ConnectorFolder> getFoldersForCourse(
      Connector connector, String username, String courseId, boolean management)
      throws LmsUserNotFoundException {
    // FIXME: courses for current user...?
    final String url = API_ROOT_V1 + "courses/" + courseId + "/contents";

    return retrieveFolders(connector, url, username, courseId, management);
  }

  @Override
  public List<ConnectorFolder> getFoldersForFolder(
      Connector connector, String username, String courseId, String folderId, boolean management)
      throws LmsUserNotFoundException {
    // FIXME: courses for current user...?
    final String url = API_ROOT_V1 + "courses/" + courseId + "/contents/" + folderId + "/children/";

    return retrieveFolders(connector, url, username, courseId, management);
  }

  private List<ConnectorFolder> retrieveFolders(
      Connector connector, String url, String username, String courseId, boolean management) {
    final List<ConnectorFolder> list = new ArrayList<>();
// FIXME: courses for current user...?
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
    // TODO CB:  Is there a better way to get the name of the folder and the course?
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
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Sending " + prettyJson(body));
      }
      // attach cached token. (Cache knows how to get a new one)
	  final String authHeaderValue = "Bearer " + getToken(connector);
      LOGGER.trace("Setting Authorization header to [" + authHeaderValue + "].  Connector [" + connector.getUuid() + "]");
      request.addHeader("Authorization", authHeaderValue);

      try (Response response =
          httpService.getWebContent(request, configService.getProxyDetails())) {
        final String responseBody = response.getBody();
        captureBlackboardRateLimitMetrics(uri.toString(), response);
        final int code = response.getCode();
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Received from Blackboard (" + code + "):");
          LOGGER.debug(prettyJson(responseBody));
        }
        if (code == 401 && firstTime) {
          // Unauthorized request.  Retry once to obtain a new token (assumes the current token is
          // expired)
          LOGGER.debug(
              "Received a 401 from Blackboard.  Token for connector ["
                  + connector.getUuid()
                  + "] is likely expired.  Retrying...");
          setToken(connector, null);
          setUserId(connector, null);
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

  public String getToken(Connector connector) {
	  return getCachedSessionValue(connector, BlackboardRESTConnectorConstants.SESSION_TOKEN);
  }

  public String getUserId(Connector connector) {
  	return getCachedSessionValue(connector, BlackboardRESTConnectorConstants.SESSION_KEY_USER_ID);
  }

  public String getUserIdType() {
	// According to the Bb Support team, accessing the REST APIs in this manner should always return a userid as a uuid
  	return "uuid:";
  }

	public void setToken(Connector connector, String token) {
		setCachedSessionValue(connector, BlackboardRESTConnectorConstants.SESSION_TOKEN, token);
	}

	public void setUserId(Connector connector, String userId) {
		setCachedSessionValue(connector, BlackboardRESTConnectorConstants.SESSION_KEY_USER_ID, userId);
	}

  private String getCachedSessionValue(Connector connector, String key) {
	final String cachedValue = userSessionService.getAttribute(connector.getUuid()+key);
	if (cachedValue == null) {
	  LOGGER.debug("No user session " + key + " for Bb REST connector [" + connector.getUuid() + "]");
	  throw new AuthenticationException("User was not able to obtain REST auth " + key + ".");
	}
	  logSensitiveDetails("Found a user session " + key + " for Bb REST connector [" + connector.getUuid() + "]", " - value [" + cachedValue + "]");
	  return cachedValue;
  }


	private void setCachedSessionValue(Connector connector, String key, String value) {
		logSensitiveDetails("Setting user session " + key + " for Bb REST connector [" + connector.getUuid() + "]", " to [" + value + "]");
		userSessionService.setAttribute(connector.getUuid()+key, value);
	}

	private void logSensitiveDetails(String msg, String sensitiveMsg) {
		if(LOGGER.isTraceEnabled()) {
			// NOTE:  Use with care - exposes sensitive details.  Only to be used for investigations
			LOGGER.trace(msg + sensitiveMsg);
		} else {
  			LOGGER.debug(msg);
		}
	}
}
