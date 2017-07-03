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

package com.tle.core.echo.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.i18n.LangUtils;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.echo.EchoConstants;
import com.tle.core.echo.dao.EchoDao;
import com.tle.core.echo.entity.EchoServer;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.common.usermanagement.user.CurrentUser;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;

@SuppressWarnings("nls")
@Bind(EchoService.class)
@Singleton
@SecureEntity("ECHO")
public class EchoServiceImpl extends AbstractEntityServiceImpl<EntityEditingBean, EchoServer, EchoService>
	implements
		EchoService
{
	@Inject
	private ObjectMapperService objectMapperService;

	private ObjectMapper mapper;

	protected final EchoDao echoDao;

	@Inject
	public EchoServiceImpl(EchoDao dao)
	{
		super(Node.ECHO, dao);
		echoDao = dao;
	}

	@Override
	public synchronized ObjectMapper getMapper()
	{
		if( mapper == null )
		{
			mapper = objectMapperService.createObjectMapper("rest");
		}
		return mapper;
	}

	@Override
	protected void doValidation(EntityEditingSession<EntityEditingBean, EchoServer> session, EchoServer es,
		List<ValidationError> errors)
	{
		addIfEmpty(errors, LangUtils.isEmpty(es.getName()), "name");
		boolean appUrlEmpty = Check.isEmpty(es.getApplicationUrl());
		addIfEmpty(errors, appUrlEmpty, "applicationurl");
		if( !appUrlEmpty )
		{
			addIfInvalid(errors, !validateUrl(es.getApplicationUrl()), "applicationurl");
		}
		boolean contentUrlEmpty = Check.isEmpty(es.getContentUrl());
		addIfEmpty(errors, contentUrlEmpty, "contenturl");
		if( !contentUrlEmpty )
		{
			addIfInvalid(errors, !validateUrl(es.getContentUrl()), "contenturl");
		}
		addIfEmpty(errors, Check.isEmpty(es.getConsumerKey()), "consumerkey");
		addIfEmpty(errors, Check.isEmpty(es.getConsumerSecret()), "consumersecret");
		addIfEmpty(errors, Check.isEmpty(es.getEchoSystemID()), "systemid");
	}

	private void addIfEmpty(List<ValidationError> errors, boolean empty, String field)
	{
		if( empty )
		{
			errors.add(new ValidationError(field, "mandatory"));
		}
	}

	private void addIfInvalid(List<ValidationError> errors, boolean empty, String field)
	{
		if( empty )
		{
			errors.add(new ValidationError(field, "invalid"));
		}
	}

	private boolean validateUrl(String url)
	{
		try
		{
			@SuppressWarnings("unused")
			URL u = new URL(url);
		}
		catch( MalformedURLException e )
		{
			return false;
		}
		return true;
	}

	@Override
	public boolean canDelete(BaseEntityLabel server)
	{
		return canDelete((Object) server);
	}

	@Override
	public boolean canDelete(EchoServer server)
	{
		return canDelete((Object) server);
	}

	private boolean canDelete(Object server)
	{
		return checkPrivs(server, Sets.newHashSet(EchoConstants.PRIV_DELETE_ECHO));
	}

	@Override
	public boolean canEdit(BaseEntityLabel server)
	{
		return canEdit((Object) server);
	}

	@Override
	public boolean canEdit(EchoServer server)
	{
		return canEdit((Object) server);
	}

	private boolean canEdit(Object server)
	{
		return checkPrivs(server, Sets.newHashSet(EchoConstants.PRIV_EDIT_ECHO));
	}

	private boolean checkPrivs(Object server, Set<String> privs)
	{
		return !aclManager.filterNonGrantedPrivileges(server, privs).isEmpty();
	}

	@Override
	@SecureOnReturn(priv = EchoConstants.PRIV_EDIT_ECHO)
	public EchoServer getForEdit(String uuid)
	{
		return getByUuid(uuid);
	}

	@Override
	public String addEchoServer(EchoServer es) throws InvalidDataException
	{
		EntityPack<EchoServer> pack = new EntityPack<EchoServer>();
		pack.setEntity(es);
		return add(pack, false).getUuid();
	}

	@Override
	@Transactional
	public void editEchoServer(String uuid, EchoServer newServer) throws InvalidDataException
	{
		EchoServer oldServer = getForEdit(uuid);

		// Common details
		editCommonFields(oldServer, newServer);

		// Other details
		oldServer.setApplicationUrl(newServer.getApplicationUrl());
		oldServer.setContentUrl(newServer.getContentUrl());
		oldServer.setConsumerKey(newServer.getConsumerKey());
		oldServer.setConsumerSecret(newServer.getConsumerSecret());
		oldServer.setEchoSystemID(newServer.getEchoSystemID());

		// Validate
		validate(null, oldServer);

		echoDao.update(oldServer);
	}

	public EchoServer getBySystemID(String esid)
	{
		return echoDao.getBySystemID(CurrentInstitution.get(), esid);
	}

	@Override
	public String getAuthenticatedUrl(String esid, String redirectURL)
	{
		EchoServer es = getBySystemID(esid);
		if( es != null )
		{
			OAuthConsumer consumer = new OAuthConsumer(null, es.getConsumerKey(), es.getConsumerSecret(), null);
			OAuthAccessor oauthAccessor = new OAuthAccessor(consumer);
			Map<String, String> params = Maps.newHashMap();
			params.put("redirecturl", redirectURL);

			try
			{
				OAuthMessage oam = oauthAccessor.newRequestMessage(OAuthMessage.GET,
					es.getContentUrl() + "ess/personapi/v1/" + CurrentUser.getUsername() + "/session",
					params.entrySet());

				return oam.URL + "?" + Joiner.on("&").join(oam.getParameters());
			}
			catch( OAuthException e )
			{
				throw new RuntimeException(e);
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
			catch( URISyntaxException e )
			{
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}