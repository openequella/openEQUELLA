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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.oauth.service.OAuthService;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;
import com.tle.web.api.oauth.OAuthEditorImpl.OAuthEditorFactory;
import com.tle.web.api.oauth.interfaces.beans.OAuthClientBean;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class OAuthBeanSerializer extends AbstractEquellaBaseEntitySerializer<OAuthClient, OAuthClientBean, OAuthEditor>
{
	@Inject
	private OAuthService oauthClientService;
	@Inject
	private OAuthEditorFactory editorFactory;
	@Inject
	private EncryptionService encryptionService;

	@Override
	protected OAuthClientBean createBean()
	{
		return new OAuthClientBean();
	}

	@Override
	protected OAuthClient createEntity()
	{
		return new OAuthClient();
	}

	@Override
	protected OAuthEditor createExistingEditor(OAuthClient entity, String stagingUuid, String lockId, boolean importing)
	{
		return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true, importing);
	}

	@Override
	protected OAuthEditor createNewEditor(OAuthClient entity, String stagingUuid, boolean importing)
	{
		return editorFactory.createNewEditor(entity, stagingUuid, importing);
	}

	@Override
	protected void copyCustomFields(OAuthClient client, OAuthClientBean bean, Object data)
	{
		bean.setClientId(client.getClientId());
		// We really shouldn't be doing this...
		bean.setClientSecret(encryptionService.decrypt(client.getClientSecret()));
		bean.setRedirectUrl(client.getRedirectUrl());
		bean.setUserId(client.getUserId());
	}

	@Override
	protected AbstractEntityService<?, OAuthClient> getEntityService()
	{
		return oauthClientService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.OAUTH_CLIENT;
	}
}
