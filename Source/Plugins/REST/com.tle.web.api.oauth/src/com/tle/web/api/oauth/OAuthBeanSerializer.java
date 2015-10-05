package com.tle.web.api.oauth;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.services.entity.AbstractEntityService;
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
		bean.setClientSecret(client.getClientSecret());
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
