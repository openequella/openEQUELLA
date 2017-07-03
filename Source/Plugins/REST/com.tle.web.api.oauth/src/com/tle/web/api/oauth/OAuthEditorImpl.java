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

package com.tle.web.api.oauth;

import java.util.Objects;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.BindFactory;
import com.tle.core.oauth.service.OAuthService;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;
import com.tle.web.api.oauth.interfaces.beans.OAuthClientBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public class OAuthEditorImpl extends AbstractBaseEntityEditor<OAuthClient, OAuthClientBean> implements OAuthEditor
{
	@Inject
	private OAuthService oauthService;
	@Inject
	private EncryptionService encryptionService;

	private boolean mustDeleteTokens;

	@AssistedInject
	public OAuthEditorImpl(@Assisted OAuthClient entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("lockId") @Nullable String lockId, @Assisted("editing") boolean editing,
		@Assisted("importing") boolean importing)
	{
		super(entity, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public OAuthEditorImpl(@Assisted OAuthClient entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("importing") boolean importing)
	{
		this(entity, stagingUuid, null, false, importing);
	}

	@Override
	protected void copyCustomFields(OAuthClientBean bean)
	{
		super.copyCustomFields(bean);

		if( !Objects.equals(bean.getClientId(), entity.getClientId())
			|| !Objects.equals(bean.getClientSecret(), entity.getClientSecret())
			|| !Objects.equals(bean.getRedirectUrl(), entity.getRedirectUrl())
			|| !Objects.equals(bean.getUserId(), entity.getUserId()) )
		{
			mustDeleteTokens = true;
		}
		entity.setClientId(bean.getClientId());
		entity.setClientSecret(encryptionService.encrypt(bean.getClientSecret()));
		entity.setPermissions(bean.getPermissions());
		entity.setUserId(bean.getUserId());
		entity.setRedirectUrl(bean.getRedirectUrl());

		// entity.setAttribute(KEY_OAUTH_FLOW, bean.getFlow());
	}

	@Override
	protected void afterFinishedEditing()
	{
		super.afterFinishedEditing();
		if( editing && mustDeleteTokens )
		{
			oauthService.deleteTokens(entity);
		}
	}

	@Override
	protected AbstractEntityService<?, OAuthClient> getEntityService()
	{
		return oauthService;
	}

	@BindFactory
	public interface OAuthEditorFactory
	{
		OAuthEditorImpl createExistingEditor(@Assisted OAuthClient oauthClient,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		OAuthEditorImpl createNewEditor(@Assisted OAuthClient oauthClient,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("importing") boolean importing);
	}
}
