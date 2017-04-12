package com.tle.web.api.oauth;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.collection.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.oauth.interfaces.OAuthResource;
import com.tle.web.api.oauth.interfaces.beans.OAuthClientBean;

/**
 * @author larry
 */
@Bind(OAuthResource.class)
@Singleton
public class OAuthResourceImpl extends AbstractBaseEntityResource<OAuthClient, BaseEntitySecurityBean, OAuthClientBean>
	implements
		OAuthResource
{
	@Inject
	private OAuthService oauthService;
	@Inject
	private OAuthBeanSerializer serializer;

	// WTF, tests expect *heavy* results on the list endpoint
	@Override
	protected OAuthClientBean serialize(OAuthClient entity, Object data, boolean heavy)
	{
		return super.serialize(entity, data, true);
	}

	@Override
	protected int getSecurityPriority()
	{
		return SecurityConstants.PRIORITY_OAUTH_CLIENT;
	}

	@Override
	protected AbstractEntityService<?, OAuthClient> getEntityService()
	{
		return oauthService;
	}

	@Override
	protected BaseEntitySerializer<OAuthClient, OAuthClientBean> getSerializer()
	{
		return serializer;
	}

	@Override
	protected Class<?> getResourceClass()
	{
		return OAuthResource.class;
	}

	@Override
	protected Node[] getAllNodes()
	{
		return new Node[]{Node.ALL_OAUTH_CLIENTS};
	}

	@Override
	protected BaseEntitySecurityBean createAllSecurityBean()
	{
		return new BaseEntitySecurityBean();
	}
}
