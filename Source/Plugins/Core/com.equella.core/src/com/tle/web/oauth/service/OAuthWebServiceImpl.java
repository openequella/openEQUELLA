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

package com.tle.web.oauth.service;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.tle.beans.Institution;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.oauth.OAuthConstants;
import com.tle.core.oauth.OAuthUserState;
import com.tle.core.oauth.event.DeleteOAuthTokensEvent;
import com.tle.core.oauth.event.listener.DeleteOAuthTokensEventListener;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.replicatedcache.ReplicatedCacheService;
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache;
import com.tle.core.services.user.UserService;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.UserState;
import com.tle.web.oauth.OAuthException;
import com.tle.web.oauth.OAuthUserStateImpl;
import com.tle.web.oauth.OAuthWebConstants;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.signature.OAuthSignatureMethod;

/**
 * @author Aaron
 */
@SuppressWarnings({"nls"})
@Bind(OAuthWebService.class)
@Singleton
public class OAuthWebServiceImpl implements OAuthWebService, DeleteOAuthTokensEventListener
{
	private static final long DEFAULT_MAX_TIMESTAMP_AGE = TimeUnit.MINUTES.toMillis(5);

	private static final String PREFIX = "com.tle.web.oauth.";
	private static final String KEY_CODE_NOT_FOUND = PREFIX + "oauth.error.codenotfound";
	private static final String KEY_CLIENT_CODE_MISMATCH = PREFIX + "oauth.error.clientcodemismatch";
	private static final String KEY_INVALID_SECRET = PREFIX + "oauth.error.invalidsecret";
	private static final String KEY_TOKEN_NOT_FOUND = PREFIX + "oauth.error.tokennotfound";

	@Inject
	private OAuthService oauthService;
	@Inject
	private UserService userService;
	@Inject
	private LanguageService languageService;
	@Inject
	private EncryptionService encryptionService;

	private ReplicatedCache<Boolean> oAuthNonceCache;
	private ReplicatedCache<CodeReg> oAuthCodesCache;

	// Not cluster safe, but that's ok, we will re-evaluate ACLs if the user
	// hits another node. The first key is institution uniqueId, second token
	// key.
	private InstitutionCache<Cache<String, OAuthUserState>> userStateMap;

	@Inject
	public void setReplicatedCache(ReplicatedCacheService service)
	{
		oAuthNonceCache = service.getCache("OAUTH_NONCES", 2000, 10, TimeUnit.MINUTES);
		oAuthCodesCache = service.getCache("OAUTH_CODES", 50000, 2, TimeUnit.MINUTES);
	}

	@Inject
	public void setInstitutionService(InstitutionService service)
	{
		userStateMap = service.newInstitutionAwareCache(new CacheLoader<Institution, Cache<String, OAuthUserState>>()
		{
			@Override
			public Cache<String, OAuthUserState> load(Institution key) throws Exception
			{
				return CacheBuilder.newBuilder().concurrencyLevel(10).maximumSize(50000)
					.expireAfterWrite(30, TimeUnit.MINUTES).build();
			}
		});
	}

	@Override
	public String createCode(OAuthClient client, AuthorisationDetails user)
	{
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
	public AuthorisationDetails getAuthorisationDetailsByCode(OAuthClient client, String code)
	{
		// code must be in the map
		Optional<CodeReg> codeOptional = oAuthCodesCache.get(code);
		if( !codeOptional.isPresent() )
		{
			throw new OAuthException(400, OAuthConstants.ERROR_INVALID_GRANT, CurrentLocale.get(KEY_CODE_NOT_FOUND));
		}

		CodeReg codeReg = codeOptional.get();
		// make sure the code came from the same client
		if( !client.getClientId().equals(codeReg.getClientId())
			|| !Objects.equals(client.getRedirectUrl(), codeReg.getRedirectUrl()) )
		{
			throw new OAuthException(400, OAuthConstants.ERROR_UNAUTHORIZED_CLIENT,
				CurrentLocale.get(KEY_CLIENT_CODE_MISMATCH, client.getClientId()), true);
		}

		final AuthorisationDetails auth = new AuthorisationDetails();
		auth.setUserId(codeReg.getUserId());
		auth.setUsername(codeReg.getUsername());
		return auth;
	}

	@Override
	public AuthorisationDetails getAuthorisationDetailsBySecret(OAuthClient client, String clientSecret)
	{
		if( clientSecret != null && !encryptionService.decrypt(client.getClientSecret()).equals(clientSecret) )
		{
			throw new OAuthException(400, OAuthConstants.ERROR_INVALID_GRANT, CurrentLocale.get(KEY_INVALID_SECRET));
		}

		final AuthorisationDetails auth = new AuthorisationDetails();
		auth.setUserId(client.getUserId());
		auth.setUsername(null);
		return auth;
	}

	@Override
	public AuthorisationDetails getAuthorisationDetailsByUserState(OAuthClient client, UserState userState)
	{
		final AuthorisationDetails auth = new AuthorisationDetails();
		if( userState.isGuest() )
		{
			auth.setRequiresLogin(true);
			return auth;
		}
		final UserBean userBean = userState.getUserBean();
		auth.setUserId(userBean.getUniqueID());
		auth.setUsername(userBean.getUsername());
		return auth;
	}

	@Override
	public OAuthUserState getUserState(String tokenData, HttpServletRequest request)
	{
		Cache<String, OAuthUserState> userCache = getUserCache(CurrentInstitution.get());
		OAuthUserState oauthUserState = userCache.getIfPresent(tokenData);
		if( oauthUserState == null )
		{
			// find the token and the user associated with it
			final OAuthToken token = oauthService.getToken(tokenData);
			if( token == null )
			{
				// FIXME: Need to fall back on server language since
				// LocaleEncodingFilter has not run yet...
				throw new OAuthException(403, OAuthConstants.ERROR_ACCESS_DENIED, languageService
					.getResourceBundle(Locale.getDefault(), "resource-centre").getString(KEY_TOKEN_NOT_FOUND));
			}

			final UserState userState = userService.authenticateAsUser(token.getUsername(),
				userService.getWebAuthenticationDetails(request));

			oauthUserState = new OAuthUserStateImpl(userState, token);
			userCache.put(tokenData, oauthUserState);
		}
		return (OAuthUserState) oauthUserState.clone();
	}

	private Cache<String, OAuthUserState> getUserCache(Institution institution)
	{
		return userStateMap.getCache(institution);
	}

	@Override
	public boolean invalidateCode(String code)
	{
		oAuthCodesCache.invalidate(code);
		return oAuthCodesCache.get(code).isPresent();
	}

	@Override
	public void deleteOAuthTokensEvent(DeleteOAuthTokensEvent event)
	{
		Cache<String, OAuthUserState> userCache = getUserCache(CurrentInstitution.get());
		userCache.invalidateAll(event.getTokens());
	}

	@Override
	public void validateMessage(OAuthMessage message, OAuthAccessor accessor)
		throws net.oauth.OAuthException, IOException, URISyntaxException
	{
		checkSingleParameters(message); // No duplicates
		validateVersion(message); // Is OAuth 1
		validateTimestampAndNonce(message);
		validateSignature(message, accessor);
	}

	/**
	 * Throw an exception if any SINGLE_PARAMETERS occur repeatedly.
	 */
	private void checkSingleParameters(OAuthMessage message) throws IOException, net.oauth.OAuthException
	{
		// Check for repeated oauth_ parameters:
		boolean repeated = false;
		Map<String, Collection<String>> nameToValues = new HashMap<String, Collection<String>>();
		for( Map.Entry<String, String> parameter : message.getParameters() )
		{
			String name = parameter.getKey();
			if( OAuthWebConstants.SINGLE_PARAMETERS.contains(name) )
			{
				Collection<String> values = nameToValues.get(name);
				if( values == null )
				{
					values = new ArrayList<String>();
					nameToValues.put(name, values);
				}
				else
				{
					repeated = true;
				}
				values.add(parameter.getValue());
			}
		}
		if( repeated )
		{
			Collection<OAuth.Parameter> rejected = new ArrayList<OAuth.Parameter>();
			for( Map.Entry<String, Collection<String>> p : nameToValues.entrySet() )
			{
				String name = p.getKey();
				Collection<String> values = p.getValue();
				if( values.size() > 1 )
				{
					for( String value : values )
					{
						rejected.add(new OAuth.Parameter(name, value));
					}
				}
			}
			OAuthProblemException problem = new OAuthProblemException(OAuth.Problems.PARAMETER_REJECTED);
			problem.setParameter(OAuth.Problems.OAUTH_PARAMETERS_REJECTED, OAuth.formEncode(rejected));
			throw problem;
		}
	}

	private void validateVersion(OAuthMessage message) throws net.oauth.OAuthException, IOException
	{
		// Checks OAuth is version 1
		String versionString = message.getParameter(OAuth.OAUTH_VERSION);
		if( versionString != null )
		{
			double version = Double.parseDouble(versionString);
			if( version < 1.0 || 1.0 < version )
			{
				OAuthProblemException problem = new OAuthProblemException(OAuth.Problems.VERSION_REJECTED);
				problem.setParameter(OAuth.Problems.OAUTH_ACCEPTABLE_VERSIONS, 1.0 + "-" + 1.0);
				throw problem;
			}
		}
	}

	/**
	 * Throw an exception if the timestamp is out of range or the nonce has been
	 * validated previously.
	 */
	private void validateTimestampAndNonce(OAuthMessage message) throws IOException, net.oauth.OAuthException
	{
		message.requireParameters(OAuth.OAUTH_TIMESTAMP, OAuth.OAUTH_NONCE);
		long timestamp = Long.parseLong(message.getParameter(OAuth.OAUTH_TIMESTAMP));
		long now = System.currentTimeMillis();
		validateTimestamp(timestamp, now);
		validateNonce(message);
	}

	/**
	 * Throw an exception if the timestamp [sec] is out of range.
	 */
	private void validateTimestamp(long timestamp, long currentTimeMsec) throws OAuthProblemException
	{
		long min = (currentTimeMsec - DEFAULT_MAX_TIMESTAMP_AGE + 500) / 1000L;
		long max = (currentTimeMsec + DEFAULT_MAX_TIMESTAMP_AGE + 500) / 1000L;
		if( timestamp < min || max < timestamp )
		{
			OAuthProblemException problem = new OAuthProblemException(OAuth.Problems.TIMESTAMP_REFUSED);
			problem.setParameter(OAuth.Problems.OAUTH_ACCEPTABLE_TIMESTAMPS, min + "-" + max);
			throw problem;
		}
	}

	/**
	 * Check if Nonce has been used previously and throw an exception if it has
	 * otherwise store it
	 * 
	 * @throws IOException, net.oauth.OAuthException
	 */
	private void validateNonce(OAuthMessage message) throws net.oauth.OAuthException, IOException
	{
		// Check if OAuth nonce is used...
		String nonce = message.getParameter(OAuth.OAUTH_NONCE);
		if( oAuthNonceCache.get(nonce).isPresent() )
		{
			throw new OAuthProblemException(OAuth.Problems.NONCE_USED);
		}

		oAuthNonceCache.put(nonce, Boolean.TRUE);
	}

	private void validateSignature(OAuthMessage message, OAuthAccessor accessor)
		throws net.oauth.OAuthException, IOException, URISyntaxException
	{
		message.requireParameters(OAuth.OAUTH_CONSUMER_KEY, OAuth.OAUTH_SIGNATURE_METHOD, OAuth.OAUTH_SIGNATURE);
		OAuthSignatureMethod.newSigner(message, accessor).validate(message);
	}

	private static class CodeReg implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String clientId;
		private String redirectUrl;
		private String userId;
		private String username;

		public String getClientId()
		{
			return clientId;
		}

		public void setClientId(String clientId)
		{
			this.clientId = clientId;
		}

		public String getRedirectUrl()
		{
			return redirectUrl;
		}

		public void setRedirectUrl(String redirectUrl)
		{
			this.redirectUrl = redirectUrl;
		}

		public String getUserId()
		{
			return userId;
		}

		public void setUserId(String userId)
		{
			this.userId = userId;
		}

		public String getUsername()
		{
			return username;
		}

		public void setUsername(String username)
		{
			this.username = username;
		}
	}
}
