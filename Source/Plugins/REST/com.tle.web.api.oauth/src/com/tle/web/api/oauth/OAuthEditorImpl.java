package com.tle.web.api.oauth;

import java.util.Objects;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.guice.BindFactory;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.services.entity.AbstractEntityService;
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

	private boolean mustDeleteTokens;

	@AssistedInject
	public OAuthEditorImpl(@Assisted OAuthClient entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("lockId") @Nullable String lockId, @Assisted boolean editing)
	{
		super(entity, stagingUuid, lockId, editing);
	}

	@AssistedInject
	public OAuthEditorImpl(@Assisted OAuthClient entity, @Assisted("stagingUuid") @Nullable String stagingUuid)
	{
		this(entity, stagingUuid, null, false);
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
		entity.setClientSecret(bean.getClientSecret());
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
		@Nullable
		OAuthEditorImpl createExistingEditor(@Assisted OAuthClient oauthClient,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted boolean editing);

		OAuthEditorImpl createNewEditor(@Assisted OAuthClient oauthClient,
			@Assisted("stagingUuid") @Nullable String stagingUuid);
	}
}
