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

package com.tle.web.oauth.service;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.CoreStrings;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.oauth.OAuthConstants;
import com.tle.core.oauth.event.DeleteOAuthTokensEvent;
import com.tle.core.oauth.event.listener.DeleteOAuthTokensEventListener;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.oauthserver.OAuthServerAccess;
import com.tle.core.replicatedcache.ReplicatedCacheService;
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache;
import com.tle.core.services.user.UserService;
import com.tle.web.oauth.OAuthException;
import com.tle.web.oauth.OAuthWebConstants;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.signature.OAuthSignatureMethod;

/** @author Aaron */
@SuppressWarnings({"nls"})
@Bind(OAuthWebService.class)
@Singleton
public class OAuthWebServiceImpl implements OAuthWebService, DeleteOAuthTokensEventListener {
  private static final long DEFAULT_MAX_TIMESTAMP_AGE = TimeUnit.MINUTES.toMillis(5);

  private static final String KEY_CODE_NOT_FOUND = "oauth.error.codenotfound";
  private static final String KEY_CLIENT_CODE_MISMATCH = "oauth.error.clientcodemismatch";
  private static final String KEY_INVALID_SECRET = "oauth.error.invalidsecret";

  @Inject private OAuthService oauthService;
  @Inject private UserService userService;
  @Inject private LanguageService languageService;
  @Inject private EncryptionService encryptionService;

  protected String text(String key, Object... vals) {
    return CoreStrings.lookup().getString(key, vals);
  }

  private ReplicatedCache<Boolean> oAuthNonceCache;
  private ReplicatedCache<CodeReg> oAuthCodesCache;

  // Not cluster safe, but that's ok, we will re-evaluate ACLs if the user
  // hits another node. The first key is institution uniqueId, second token
  // key.
  private InstitutionCache<Cache<String, UserState>> userStateMap;

  @Inject
  public void setReplicatedCache(ReplicatedCacheService service) {
    oAuthNonceCache = service.getCache("OAUTH_NONCES", 2000, 10, TimeUnit.MINUTES);
    oAuthCodesCache = service.getCache("OAUTH_CODES", 50000, 2, TimeUnit.MINUTES);
  }

  @Inject
  public void setInstitutionService(InstitutionService service) {
    userStateMap =
        service.newInstitutionAwareCache(
            new CacheLoader<Institution, Cache<String, UserState>>() {
              @Override
              public Cache<String, UserState> load(Institution key) throws Exception {
                return CacheBuilder.newBuilder()
                    .concurrencyLevel(10)
                    .maximumSize(50000)
                    .expireAfterWrite(30, TimeUnit.MINUTES)
                    .build();
              }
            });
  }

  @Override
  public String createCode(OAuthClient client, AuthorisationDetails user) {
    final CodeReg codeReg = new CodeReg();
    codeReg.setClientId(client.getClientId());
    codeReg.setRedirectUrl(client.getRedirectUrl());
    codeReg.setUserId(user.getUserId());
    codeReg.setUsername(user.getUsername());

    String uuid = UUID.randomUUID().toString();
    oAuthCodesCache.put(uuid, codeReg);
    return uuid;
  }

  @Override
  public AuthorisationDetails getAuthorisationDetailsByCode(IOAuthClient client, String code) {
    // code must be in the map
    Optional<CodeReg> codeOptional = oAuthCodesCache.get(code);
    if (!codeOptional.isPresent()) {
      throw new OAuthException(400, OAuthConstants.ERROR_INVALID_GRANT, text(KEY_CODE_NOT_FOUND));
    }

    CodeReg codeReg = codeOptional.get();
    // make sure the code came from the same client
    if (!client.getClientId().equals(codeReg.getClientId())
        || !Objects.equals(client.getRedirectUrl(), codeReg.getRedirectUrl())) {
      throw new OAuthException(
          400,
          OAuthConstants.ERROR_UNAUTHORIZED_CLIENT,
          text(KEY_CLIENT_CODE_MISMATCH, client.getClientId()),
          true);
    }

    final AuthorisationDetails auth = new AuthorisationDetails();
    auth.setUserId(codeReg.getUserId());
    auth.setUsername(codeReg.getUsername());
    return auth;
  }

  @Override
  public AuthorisationDetails getAuthorisationDetailsBySecret(
      IOAuthClient client, String clientSecret) {
    if (clientSecret != null && !client.secretMatches(clientSecret)) {
      throw new OAuthException(400, OAuthConstants.ERROR_INVALID_GRANT, text(KEY_INVALID_SECRET));
    }

    final AuthorisationDetails auth = new AuthorisationDetails();
    auth.setUserId(client.getUserId());
    auth.setUsername(null);
    return auth;
  }

  @Override
  public AuthorisationDetails getAuthorisationDetailsByUserState(
      OAuthClient client, UserState userState) {
    final AuthorisationDetails auth = new AuthorisationDetails();
    if (userState.isGuest()) {
      auth.setRequiresLogin(true);
      return auth;
    }
    final UserBean userBean = userState.getUserBean();
    auth.setUserId(userBean.getUniqueID());
    auth.setUsername(userBean.getUsername());
    return auth;
  }

  @Override
  public UserState getUserState(String tokenData, HttpServletRequest request) {
    Cache<String, UserState> userCache = getUserCache(CurrentInstitution.get());
    UserState oauthUserState = userCache.getIfPresent(tokenData);
    if (oauthUserState == null) {
      // find the token and the user associated with it
      oauthUserState = OAuthServerAccess.findUserState(tokenData, request);
      userCache.put(tokenData, oauthUserState);
    }

    try {
      return oauthUserState.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  private Cache<String, UserState> getUserCache(Institution institution) {
    return userStateMap.getCache(institution);
  }

  @Override
  public boolean invalidateCode(String code) {
    oAuthCodesCache.invalidate(code);
    return oAuthCodesCache.get(code).isPresent();
  }

  @Override
  public void deleteOAuthTokensEvent(DeleteOAuthTokensEvent event) {
    Cache<String, UserState> userCache = getUserCache(CurrentInstitution.get());
    userCache.invalidateAll(event.getTokens());
  }

  @Override
  public void validateMessage(OAuthMessage message, OAuthAccessor accessor)
      throws net.oauth.OAuthException, IOException, URISyntaxException {
    checkSingleParameters(message); // No duplicates
    validateVersion(message); // Is OAuth 1
    validateTimestampAndNonce(message);
    validateSignature(message, accessor);
  }

  @Override
  public IOAuthToken getOrCreateToken(
      AuthorisationDetails authDetails, IOAuthClient client, String code) {
    return OAuthServerAccess.getOrCreateToken(authDetails, client, code);
  }

  @Override
  public IOAuthClient getByClientIdOnly(String clientId) {
    return OAuthServerAccess.byClientId(clientId);
  }

  @Override
  public IOAuthClient getByClientIdAndRedirectUrl(String clientId, String redirectUrl) {
    return OAuthServerAccess.byClientIdAndRedirect(clientId, redirectUrl);
  }

  /** Throw an exception if any SINGLE_PARAMETERS occur repeatedly. */
  private void checkSingleParameters(OAuthMessage message)
      throws IOException, net.oauth.OAuthException {
    // Check for repeated oauth_ parameters:
    boolean repeated = false;
    Map<String, Collection<String>> nameToValues = new HashMap<String, Collection<String>>();
    for (Map.Entry<String, String> parameter : message.getParameters()) {
      String name = parameter.getKey();
      if (OAuthWebConstants.SINGLE_PARAMETERS.contains(name)) {
        Collection<String> values = nameToValues.get(name);
        if (values == null) {
          values = new ArrayList<String>();
          nameToValues.put(name, values);
        } else {
          repeated = true;
        }
        values.add(parameter.getValue());
      }
    }
    if (repeated) {
      Collection<OAuth.Parameter> rejected = new ArrayList<OAuth.Parameter>();
      for (Map.Entry<String, Collection<String>> p : nameToValues.entrySet()) {
        String name = p.getKey();
        Collection<String> values = p.getValue();
        if (values.size() > 1) {
          for (String value : values) {
            rejected.add(new OAuth.Parameter(name, value));
          }
        }
      }
      OAuthProblemException problem = new OAuthProblemException(OAuth.Problems.PARAMETER_REJECTED);
      problem.setParameter(OAuth.Problems.OAUTH_PARAMETERS_REJECTED, OAuth.formEncode(rejected));
      throw problem;
    }
  }

  private void validateVersion(OAuthMessage message) throws net.oauth.OAuthException, IOException {
    // Checks OAuth is version 1
    String versionString = message.getParameter(OAuth.OAUTH_VERSION);
    if (versionString != null) {
      double version = Double.parseDouble(versionString);
      if (version < 1.0 || 1.0 < version) {
        OAuthProblemException problem = new OAuthProblemException(OAuth.Problems.VERSION_REJECTED);
        problem.setParameter(OAuth.Problems.OAUTH_ACCEPTABLE_VERSIONS, 1.0 + "-" + 1.0);
        throw problem;
      }
    }
  }

  /**
   * Throw an exception if the timestamp is out of range or the nonce has been validated previously.
   */
  private void validateTimestampAndNonce(OAuthMessage message)
      throws IOException, net.oauth.OAuthException {
    message.requireParameters(OAuth.OAUTH_TIMESTAMP, OAuth.OAUTH_NONCE);
    long timestamp = Long.parseLong(message.getParameter(OAuth.OAUTH_TIMESTAMP));
    long now = System.currentTimeMillis();
    validateTimestamp(timestamp, now);
    validateNonce(message);
  }

  /** Throw an exception if the timestamp [sec] is out of range. */
  private void validateTimestamp(long timestamp, long currentTimeMsec)
      throws OAuthProblemException {
    long min = (currentTimeMsec - DEFAULT_MAX_TIMESTAMP_AGE + 500) / 1000L;
    long max = (currentTimeMsec + DEFAULT_MAX_TIMESTAMP_AGE + 500) / 1000L;
    if (timestamp < min || max < timestamp) {
      OAuthProblemException problem = new OAuthProblemException(OAuth.Problems.TIMESTAMP_REFUSED);
      problem.setParameter(OAuth.Problems.OAUTH_ACCEPTABLE_TIMESTAMPS, min + "-" + max);
      throw problem;
    }
  }

  /**
   * Check if Nonce has been used previously and throw an exception if it has otherwise store it
   *
   * @throws IOException, net.oauth.OAuthException
   */
  private void validateNonce(OAuthMessage message) throws net.oauth.OAuthException, IOException {
    // Check if OAuth nonce is used...
    String nonce = message.getParameter(OAuth.OAUTH_NONCE);
    if (oAuthNonceCache.get(nonce).isPresent()) {
      throw new OAuthProblemException(OAuth.Problems.NONCE_USED);
    }

    oAuthNonceCache.put(nonce, Boolean.TRUE);
  }

  private void validateSignature(OAuthMessage message, OAuthAccessor accessor)
      throws net.oauth.OAuthException, IOException, URISyntaxException {
    message.requireParameters(
        OAuth.OAUTH_CONSUMER_KEY, OAuth.OAUTH_SIGNATURE_METHOD, OAuth.OAUTH_SIGNATURE);
    OAuthSignatureMethod.newSigner(message, accessor).validate(message);
  }

  @Override
  public List<Map.Entry<String, String>> getOauthSignatureParams(
      String consumerKey, String secret, String urlStr, Map<String, String[]> formParams) {
    String nonce = UUID.randomUUID().toString();
    String timestamp = Long.toString(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    // OAuth likes the Map.Entry interface, so copy into a Collection of a
    // local implementation thereof. Note that this is a flat list.
    List<OAuth.Parameter> postParams = null;

    if (!Check.isEmpty(formParams)) {
      postParams = new ArrayList<OAuth.Parameter>(formParams.size());
      for (Map.Entry<String, String[]> entry : formParams.entrySet()) {
        String key = entry.getKey();
        String[] formParamEntry = entry.getValue();
        // cater for multiple values for the same key
        if (formParamEntry.length > 0) {
          for (int i = 0; i < formParamEntry.length; ++i) {
            OAuth.Parameter erp = new OAuth.Parameter(entry.getKey(), formParamEntry[i]);
            postParams.add(erp);
          }
        } else {
          // key with no value
          postParams.add(new OAuth.Parameter(key, null));
        }
      }
    }

    OAuthMessage message = new OAuthMessage(OAuthMessage.POST, urlStr, postParams);
    // Parameters needed for a signature
    message.addParameter(OAuth.OAUTH_CONSUMER_KEY, consumerKey);
    message.addParameter(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
    message.addParameter(OAuth.OAUTH_NONCE, nonce);
    message.addParameter(OAuth.OAUTH_TIMESTAMP, timestamp);
    message.addParameter(OAuth.OAUTH_VERSION, OAuth.VERSION_1_0);
    message.addParameter(OAuth.OAUTH_CALLBACK, "about:blank");

    // Sign the request
    OAuthConsumer consumer = new OAuthConsumer("about:blank", consumerKey, secret, null);
    OAuthAccessor accessor = new OAuthAccessor(consumer);
    try {
      message.sign(accessor);
      // send oauth parameters back including signature
      return message.getParameters();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static final class CodeReg implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clientId;
    private String redirectUrl;
    private String userId;
    private String username;

    public String getClientId() {
      return clientId;
    }

    public void setClientId(String clientId) {
      this.clientId = clientId;
    }

    public String getRedirectUrl() {
      return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
      this.redirectUrl = redirectUrl;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }
  }
}
