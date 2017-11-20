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

package com.tle.web.api.institution;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.base.Strings;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.ump.RoleMapping;
import com.tle.beans.usermanagement.standard.wrapper.RoleWrapperSettings;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.users.interfaces.beans.RoleBean;
import com.tle.web.remoting.rest.service.UrlLinkService;

/**
 * @author larry
 */
@NonNullByDefault
@Bind(EquellaRoleResource.class)
@Singleton
public class RoleResourceImpl implements EquellaRoleResource
{
	/**
	 * Equella uses ridiculous config properties for roles, meaning every update
	 * to a single role requires purging ALL roles from the DB and re-inserting
	 * them. This has disastrous consequences for multiple threads updating
	 * roles. This lock is an attempt to reduce the issue, however it will still
	 * be an issue with clustered installs.
	 */
	private Object roleLock = new Object();

	@Inject
	private TLEAclManager aclManager;
	@Inject
	private ConfigurationService configService;
	@Inject
	private UrlLinkService urlLinkService;

	@Override
	public RoleBean getRole(UriInfo uriInfo, String uuid)
	{
		ensurePriv();
		final RoleWrapperSettings roles = configService.getProperties(new RoleWrapperSettings());

		final RoleMapping role = getRoleMappingById(roles, uuid);
		if( role != null )
		{
			return makeRoleBean(role);
		}
		throw new NotFoundException("No role with ID " + uuid + " found");
	}

	@Override
	public Response editRole(String uuid, RoleBean roleBean)
	{
		ensurePriv();
		synchronized( roleLock )
		{
			final RoleWrapperSettings roles = configService.getProperties(new RoleWrapperSettings());

			final RoleMapping role = getRoleMappingById(roles, uuid);
			if( role != null )
			{
				role.setName(roleBean.getName());
				role.setExpression(roleBean.getExpression());
				configService.setProperties(roles);
				return Response.status(Status.OK).build();
			}
			throw new NotFoundException("No role with ID " + uuid + " found");
		}
	}

	@Override
	public Response deleteRole(String uuid)
	{
		ensurePriv();
		synchronized( roleLock )
		{
			final RoleWrapperSettings roles = configService.getProperties(new RoleWrapperSettings());
			boolean found = false;
			for( RoleMapping rm : roles.getRoles() )
			{
				if( rm.getId().equals(uuid) )
				{
					found = true;
					roles.getRoles().remove(rm);
					break;
				}
			}
			if( found )
			{
				configService.setProperties(roles);
				return Response.status(Status.NO_CONTENT).build();
			}
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Override
	public Response addRole(RoleBean role)
	{
		ensurePriv();
		String roleId = role.getId();
		if( Strings.isNullOrEmpty(roleId) )
		{
			roleId = UUID.randomUUID().toString();
		}
		String expression = role.getExpression();
		synchronized( roleLock )
		{
			RoleWrapperSettings roles = configService.getProperties(new RoleWrapperSettings());

			List<RoleMapping> currentRoles = roles.getRoles();
			for( RoleMapping currentRole : currentRoles )
			{
				if( currentRole.getId().equals(roleId) )
				{
					throw new InvalidDataException(Collections.singletonList(new ValidationError("id",
						"ID already in use")));
				}
			}

			final RoleMapping rm = new RoleMapping();
			rm.setId(roleId);
			rm.setExpression(expression);
			rm.setName(role.getName());
			currentRoles.add(rm);

			configService.setProperties(roles);
		}

		return Response.created(getSelfLink(roleId)).build();
	}

	@Override
	public RoleBean getRoleByName(UriInfo uriInfo, String name)
	{
		ensurePriv();
		final RoleWrapperSettings roles = configService.getProperties(new RoleWrapperSettings());

		final List<RoleMapping> currentRoles = roles.getRoles();
		for( RoleMapping currentRole : currentRoles )
		{
			if( currentRole.getName().equals(name) )
			{
				return makeRoleBean(currentRole);
			}
		}
		throw new NotFoundException("No role with name " + name + " found");
	}

	@Override
	public SearchBean<RoleBean> list(UriInfo uriInfo, String query)
	{
		ensurePriv();
		final List<RoleBean> roleBeans = new ArrayList<>();

		final RoleWrapperSettings roles = configService.getProperties(new RoleWrapperSettings());
		final List<RoleMapping> currentRoles = roles.getRoles();
		for( RoleMapping currentRole : currentRoles )
		{
			roleBeans.add(makeRoleBean(currentRole));
		}

		final SearchBean<RoleBean> results = new SearchBean<>();
		results.setStart(0);
		results.setLength(roleBeans.size());
		results.setAvailable(roleBeans.size());
		results.setResults(roleBeans);
		return results;
	}

	@Nullable
	private RoleMapping getRoleMappingById(RoleWrapperSettings roles, String roleId)
	{
		for( RoleMapping rm : roles.getRoles() )
		{
			if( rm.getId().equals(roleId) )
			{
				return rm;
			}
		}
		return null;
	}

	private RoleBean makeRoleBean(RoleMapping rm)
	{
		final RoleBean roleBean = new RoleBean(rm.getId());
		roleBean.setName(rm.getName());
		roleBean.setExpression(rm.getExpression());
		final Map<String, String> linkMap = new HashMap<>();
		linkMap.put("self", getSelfLink(rm.getId()).toString());
		roleBean.set("links", linkMap);
		return roleBean;
	}

	private void ensurePriv()
	{
		final Set<String> stillThere = aclManager.filterNonGrantedPrivileges("EDIT_USER_MANAGEMENT");
		if( stillThere.isEmpty() )
		{
			throw new AccessDeniedException("EDIT_USER_MANAGEMENT not granted");
		}
	}

	private URI getSelfLink(String roleId)
	{
		return urlLinkService.getMethodUriBuilder(EquellaRoleResource.class, "getRole").build(roleId);
	}
}
