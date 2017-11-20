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

package com.tle.core.oauth.service.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.oauth.OAuthConstants;
import com.tle.core.oauth.OAuthFlowDefinitions;
import com.tle.core.oauth.dao.OAuthClientDao;
import com.tle.core.oauth.dao.OAuthTokenDao;
import com.tle.core.oauth.event.DeleteOAuthTokensEvent;
import com.tle.core.oauth.event.OAuthClientReferencesEvent;
import com.tle.core.oauth.service.OAuthClientEditingBean;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.events.services.EventService;
import com.tle.core.services.ValidationHelper;
import com.tle.exceptions.AccessDeniedException;

/**
 * @author aholland
 */
@Singleton
@SuppressWarnings("nls")
@Bind(OAuthService.class)
@SecureEntity(OAuthService.ENTITY_TYPE)
public class OAuthServiceImpl extends AbstractEntityServiceImpl<OAuthClientEditingBean, OAuthClient, OAuthService>
	implements
		OAuthService
{
	private static final String KEY_MUST_DELETE_TOKENS = "$MUST$DELETE$TOKENS";
	private static final String KEY_ERROR_CANNOT_ADMINISTER_TOKENS = "com.tle.core.oauth.error.cannotadmintokens";
	private static final String KEY_ERROR_VALIDATION_CLIENT_ID_UNIQUE = "com.tle.core.oauth.error.validation.clientidunique";
	private static final String KEY_ERROR_VALIDATION_REDIRECTURL_INVALID = "com.tle.core.oauth.error.validation.redirecturlinvalid";

	public static final String KEY_OAUTH_FLOW = "oauth.flow";

	private static final String[] BLANKS = {"name", "clientId", "clientSecret"};

	@Inject
	private OAuthTokenDao tokenDao;
	@Inject
	private EventService eventService;
	@Inject
	private EncryptionService encryptionService;

	private final OAuthClientDao clientDao;

	@Inject
	public OAuthServiceImpl(OAuthClientDao dao)
	{
		super(Node.OAUTH_CLIENT, dao);
		clientDao = dao;
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected OAuthClientEditingBean createEditingBean()
	{
		return new OAuthClientEditingBean();
	}

	@SuppressWarnings("unused")
	@Override
	protected void doValidation(EntityEditingSession<OAuthClientEditingBean, OAuthClient> session, OAuthClient oauth,
		List<ValidationError> errors)
	{
		ValidationHelper.checkBlankFields(oauth, BLANKS, errors);
		final String redirect = oauth.getRedirectUrl();
		if( redirect != null && !redirect.equals("default") )
		{
			try
			{
				new URL(redirect);
			}
			catch( MalformedURLException m ) // NOSONAR
			{
				errors.add(
					new ValidationError("redirectUrl", CurrentLocale.get(KEY_ERROR_VALIDATION_REDIRECTURL_INVALID)));
			}
		}
		// check uniqueness of clientId
		final String clientId = oauth.getClientId();
		if( !Check.isEmpty(clientId) )
		{
			final OAuthClient old = getByClientIdOnly(clientId);
			if( old != null && old.getId() != oauth.getId() )
			{
				errors.add(new ValidationError("clientId", CurrentLocale.get(KEY_ERROR_VALIDATION_CLIENT_ID_UNIQUE)));
			}
		}
	}

	@SuppressWarnings("unused")
	@Override
	protected void doValidationBean(OAuthClientEditingBean bean, List<ValidationError> errors)
	{
		super.doValidationBean(bean, errors);

		ValidationHelper.checkBlankFields(bean, BLANKS, errors);
		final String redirect = bean.getRedirectUrl();
		if( redirect != null && !redirect.equals("default") )
		{
			try
			{
				new URL(redirect);
			}
			catch( MalformedURLException m ) // NOSONAR
			{
				errors.add(
					new ValidationError("redirectUrl", CurrentLocale.get(KEY_ERROR_VALIDATION_REDIRECTURL_INVALID)));
			}
		}
		// check uniqueness of clientId
		final String clientId = bean.getClientId();
		if( !Check.isEmpty(clientId) )
		{
			final OAuthClient old = getByClientIdOnly(clientId);
			if( old != null && old.getId() != bean.getId() )
			{
				errors.add(new ValidationError("clientId", CurrentLocale.get(KEY_ERROR_VALIDATION_CLIENT_ID_UNIQUE)));
			}
		}
	}

	@Override
	protected void beforeStopEdit(EntityPack<OAuthClient> pack, OAuthClient oldClient, boolean unlock)
	{
		super.beforeStopEdit(pack, oldClient, unlock);
		// mark the changes
		final OAuthClient newClient = pack.getEntity();
		if( !Objects.equals(newClient.getClientId(), oldClient.getClientId())
			|| !Objects.equals(newClient.getClientSecret(), oldClient.getClientSecret())
			|| !Objects.equals(newClient.getRedirectUrl(), oldClient.getRedirectUrl())
			|| !Objects.equals(newClient.getUserId(), oldClient.getUserId()) )
		{
			pack.setAttribute(KEY_MUST_DELETE_TOKENS, true);
		}
	}

	@Override
	protected void afterStopEdit(EntityPack<OAuthClient> pack, OAuthClient oldEntity)
	{
		super.afterStopEdit(pack, oldEntity);
		// if any field changed (except description) then delete the tokens
		final Boolean mustDeleteTokens = pack.getAttribute(KEY_MUST_DELETE_TOKENS);
		if( mustDeleteTokens != null && mustDeleteTokens )
		{
			deleteTokens(oldEntity);
		}
	}

	@Override
	protected void deleteReferences(OAuthClient client)
	{
		super.deleteReferences(client);
		deleteTokens(client);
	}

	@SecureOnCall(priv = "DELETE_OAUTH_CLIENT")
	@Override
	public void deleteTokens(OAuthClient client)
	{
		final List<String> deletedTokens = Lists.newArrayList();
		// delete all associated tokens
		for( OAuthToken token : tokenDao.findAllByClient(client) )
		{
			deletedTokens.add(token.getToken());
			tokenDao.delete(token);
		}
		eventService.publishApplicationEvent(new DeleteOAuthTokensEvent(deletedTokens));
	}

	@Override
	protected final EntityPack<OAuthClient> startEditInternal(OAuthClient entity)
	{
		ensureNonSystem(entity);
		EntityPack<OAuthClient> pack = new EntityPack<OAuthClient>();
		pack.setEntity(entity);

		// Prepare staging
		EntityFile from = new EntityFile(entity);
		StagingFile staging = stagingService.createStagingArea();
		if( fileSystemService.fileExists(from) )
		{
			fileSystemService.copy(from, staging);
		}
		pack.setStagingID(staging.getUuid());

		fillTargetLists(pack);
		return pack;
	}

	@Override
	protected void populateEditingBean(OAuthClientEditingBean bean, OAuthClient entity)
	{
		super.populateEditingBean(bean, entity);

		final OAuthClientEditingBean ocBean = bean;
		ocBean.setClientId(entity.getClientId());
		ocBean.setClientSecret(encryptionService.decrypt(entity.getClientSecret()));
		final Set<String> permissions = entity.getPermissions();
		if( permissions == null )
		{
			ocBean.setPermissions(null);
		}
		else
		{
			ocBean.setPermissions(Sets.newHashSet(permissions));
		}
		ocBean.setRedirectUrl(entity.getRedirectUrl());
		ocBean.setRequiresApproval(entity.isRequiresApproval());
		ocBean.setUserId(entity.getUserId());
		if( entity.getAttribute(KEY_OAUTH_FLOW) != null )
		{
			ocBean.setFlowDef(OAuthFlowDefinitions.getForId(entity.getAttribute(KEY_OAUTH_FLOW)));
		}
	}

	@Override
	protected void populateEntity(OAuthClientEditingBean bean, OAuthClient entity)
	{
		super.populateEntity(bean, entity);

		final OAuthClientEditingBean ocBean = bean;
		entity.setClientId(ocBean.getClientId());
		entity.setClientSecret(encryptionService.encrypt(ocBean.getClientSecret()));
		final Set<String> permissions = ocBean.getPermissions();
		if( permissions == null )
		{
			entity.setPermissions(null);
		}
		else
		{
			entity.setPermissions(Sets.newHashSet(ocBean.getPermissions()));
		}
		entity.setRedirectUrl(ocBean.getRedirectUrl());
		entity.setRequiresApproval(ocBean.isRequiresApproval());
		entity.setUserId(ocBean.getUserId());
		if( ocBean.getFlowDef() != null )
		{
			entity.setAttribute(KEY_OAUTH_FLOW, ocBean.getFlowDef().getId());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<OAuthClientEditingBean, OAuthClient>> SESSION createSession(
		String sessionId, EntityPack<OAuthClient> pack, OAuthClientEditingBean bean)
	{
		return (SESSION) new OAuthClientEditingSessionImpl(sessionId, pack, bean);
	}

	@Override
	protected void beforeClone(TemporaryFileHandle staging, EntityPack<OAuthClient> pack)
	{
		// export the prefs into the staging area
		prepareExport(staging, pack.getEntity(),
			new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));
	}

	@Override
	public boolean canEdit(BaseEntityLabel client)
	{
		return canEdit((Object) client);
	}

	@Override
	public boolean canEdit(OAuthClient client)
	{
		return canEdit((Object) client);
	}

	private boolean canEdit(Object client)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(OAuthConstants.PRIV_EDIT_OAUTH_CLIENT);
		return !aclManager.filterNonGrantedPrivileges(client, privs).isEmpty();
	}

	@Override
	public boolean canDelete(BaseEntityLabel client)
	{
		return canDelete((Object) client);
	}

	@Override
	public boolean canDelete(OAuthClient client)
	{
		return canDelete((Object) client);
	}

	private boolean canDelete(Object client)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(OAuthConstants.PRIV_DELETE_OAUTH_CLIENT);
		return !aclManager.filterNonGrantedPrivileges(client, privs).isEmpty();
	}

	@Override
	public OAuthClient getByClientIdAndRedirectUrl(String clientId, String redirectUrl)
	{
		return clientDao.getByClientIdAndRedirectUrl(clientId, redirectUrl);
	}

	/**
	 * @param clientId - assumed to be not null
	 */
	@Override
	public OAuthClient getByClientIdOnly(String clientId)
	{
		return clientDao.getByClientIdOnly(clientId);
	}

	@Transactional
	@Override
	public OAuthToken getOrCreateToken(String userId, String username, OAuthClient client, String code)
	{
		OAuthToken token = tokenDao.getToken(userId, client);
		if( token == null )
		{
			token = new OAuthToken(userId, username, UUID.randomUUID().toString(), new Date(), client,
				CurrentInstitution.get());
			token.setCode(code);
			tokenDao.save(token);
		}
		return token;
	}

	@Override
	public boolean canAdministerTokens()
	{
		final Set<String> privs = new HashSet<String>();
		privs.add(OAuthConstants.PRIV_ADMINISTER_OAUTH_TOKENS);
		return !aclManager.filterNonGrantedPrivileges(privs).isEmpty();
	}

	// @SecureOnCall(priv = OAuthConstants.PRIV_ADMINISTER_OAUTH_TOKENS)
	@Transactional
	@Override
	public boolean deleteToken(long id)
	{
		if( !canAdministerTokens() )
		{
			throw new AccessDeniedException(CurrentLocale.get(KEY_ERROR_CANNOT_ADMINISTER_TOKENS));
		}
		final OAuthToken token = tokenDao.findById(id);
		if( token != null )
		{
			final List<String> deletedTokens = Lists.newArrayList(token.getToken());
			eventService.publishApplicationEvent(new DeleteOAuthTokensEvent(deletedTokens));
			tokenDao.delete(token);
			return true;
		}
		return false;
	}

	@Override
	public List<OAuthToken> listAllTokens()
	{
		if( !canAdministerTokens() )
		{
			throw new AccessDeniedException(CurrentLocale.get(KEY_ERROR_CANNOT_ADMINISTER_TOKENS));
		}
		return tokenDao.enumerateAll();
	}

	/**
	 * Unsecured
	 */
	@Override
	public OAuthToken getToken(String tokenData)
	{
		return tokenDao.getToken(tokenData);
	}

	@Override
	public <SESSION extends EntityEditingSession<OAuthClientEditingBean, OAuthClient>> SESSION startNewSession(
		OAuthClient client)
	{
		SESSION session = super.startNewSession(client);
		OAuthClientEditingBean bean = session.getBean();
		bean.setClientSecret(UUID.randomUUID().toString());
		bean.setClientId(UUID.randomUUID().toString());
		return session;
	}

	@Override
	public XStream getXStream()
	{
		final XStream xstream = super.getXStream();
		xstream.alias("com.tle.core.oauth.beans.OAuthClient", OAuthClient.class);
		xstream.alias("com.tle.core.oauth.beans.OAuthToken", OAuthToken.class);
		return xstream;
	}

	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		final OAuthClientReferencesEvent event = new OAuthClientReferencesEvent(get(id));
		publishEvent(event);
		return event.getReferencingClasses();
	}

	@Transactional
	@Override
	public void userDeletedEvent(UserDeletedEvent event)
	{
		// The super behaviour is to orphan things and we want most OAuthClients
		// to stick around, so let owned ones be orphaned first.
		super.userDeletedEvent(event);

		// ...but, clients that are locked to the user being deleted are
		// useless, so we'll delete those.
		Criterion c1 = Restrictions.eq("userId", event.getUserID());
		Criterion c2 = Restrictions.eq("institution", CurrentInstitution.get());
		for( OAuthClient oc : clientDao.findAllByCriteria(c1, c2) )
		{
			delete(oc, false);
		}

		tokenDao.deleteAllForUser(event.getUserID());
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		clientDao.changeUserId(event.getFromUserId(), event.getToUserId());
		tokenDao.changeUserId(event.getFromUserId(), event.getToUserId());

		super.userIdChangedEvent(event);
	}
}
