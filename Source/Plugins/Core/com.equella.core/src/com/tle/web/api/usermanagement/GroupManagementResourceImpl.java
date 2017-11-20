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

package com.tle.web.api.usermanagement;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.tle.beans.user.TLEGroup;
import com.tle.common.Check;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.usermanagement.standard.service.TLEGroupService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.users.interfaces.beans.GroupBean;
import com.tle.web.remoting.rest.service.RestImportExportHelper;
import com.tle.web.remoting.rest.service.UrlLinkService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * See the interface class for the @Path annotations.
 */
@SuppressWarnings("nls")
@Bind(EquellaGroupResource.class)
@Singleton
public class GroupManagementResourceImpl implements EquellaGroupResource
{
	private static final Logger LOGGER = Logger.getLogger(GroupManagementResourceImpl.class);
	private static final String APIDOC_GROUPUUID = "The uuid of the group";

	@Inject
	private TLEAclManager aclManager;
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private TLEGroupService tleGroupService;

	/**
	 * QueryString q will be fitted with leading and trailing wildcard and
	 * matched against Group name.<br>
	 * An optional query param is userId, in which case the include all parents
	 * flag is referred to in compiling results. If queried on both name and
	 * user, and allParents flag is true, the result set may include parent
	 * groups which themselves may lie outside the query filter. If queried on
	 * both name and user, explicit setting of allparents to false will confine
	 * results to groups which match the query filter and directly contain the
	 * nominated user.
	 * 
	 * @param q
	 * @param userId
	 * @param allparents relevant only if userId provided. defaults to true.
	 * @return
	 */
	@Override
	public SearchBean<GroupBean> list(UriInfo uriInfo, String q, String userId, Boolean allParents, String name)
	{
		ensurePriv();
		SearchBean<GroupBean> result = new SearchBean<GroupBean>();
		result.setStart(0);

		if( name != null )
		{
			List<GroupBean> beans = Lists.newArrayList();
			TLEGroup groupByName = tleGroupService.getByName(name);
			if( groupByName != null )
			{
				GroupBean newB = apiGroupBeanFromTLEGroup(groupByName, false);
				beans.add(newB);
			}
			result.setLength(beans.size());
			result.setResults(beans);
			result.setAvailable(beans.size());
			return result;
		}

		final List<TLEGroup> rawResults;
		// If userId value exists, use a variant tleGroupService call
		// if the query is null, leave it as a non-null empty string, otherwise
		// wrap it as a wildcard
		final String query = (q == null ? "*" : tleGroupService.prepareQuery(q));
		if( !Check.isEmpty(userId) )
		{
			// if no allParents param exists, impose true.
			boolean inclusive = (allParents == null ? true : allParents);
			rawResults = tleGroupService.search(query, userId, inclusive);
		}
		else
		{
			// prepareQuery doesn't like null argument
			rawResults = tleGroupService.search(query);
		}

		// If this is a call to retrieve exportable data, we want include the
		// users per group.
		boolean isExport = RestImportExportHelper.isExport(uriInfo);

		List<GroupBean> resultsOfBeans = Lists.newArrayList();
		for( TLEGroup tleGroup : rawResults )
		{
			GroupBean newB = apiGroupBeanFromTLEGroup(tleGroup, isExport);
			resultsOfBeans.add(newB);
		}
		result.setStart(0);
		result.setLength(resultsOfBeans.size());
		result.setResults(resultsOfBeans);
		result.setAvailable(rawResults.size());

		return result;
	}

	@Override
	public GroupBean getGroup(UriInfo uriInfo, String uuid)
	{
		ensurePriv();
		GroupBean groupBean = null;
		TLEGroup tleGroup = tleGroupService.get(uuid);
		if( tleGroup != null )
		{
			// Why the hell does EPS return the users on the entity??
			groupBean = apiGroupBeanFromTLEGroup(tleGroup, true);
			return groupBean;
		}
		throw new NotFoundException();
	}

	@Override
	public SearchBean<UserBean> getUsersInGroup(UriInfo uriInfo, String uuid, boolean recursive)
	{
		ensurePriv();
		List<String> members = tleGroupService.getUsersInGroup(uuid, recursive);
		List<UserBean> users = Lists.newArrayList();
		for( String memberId : members )
		{
			UserBean user = new UserBean(memberId);
			Map<String, String> links = Collections.singletonMap("self",
				urlLinkService.getMethodUriBuilder(EquellaUserResource.class, "getUser").build(uuid).toString());
			user.set("links", links);
			users.add(user);
		}

		SearchBean<UserBean> results = new SearchBean<>();
		results.setStart(0);
		results.setLength(users.size());
		results.setAvailable(users.size());
		results.setResults(users);
		return results;
	}

	@Override
	public Response addGroup(GroupBean groupBean)
	{
		try
		{
			String uuid = groupBean.getId();
			if( Check.isEmpty(uuid) )
			{
				uuid = UUID.randomUUID().toString();
				groupBean.setId(uuid);
			}
			String surelythesameuuid = tleGroupService.add(populateTLEGroup(groupBean));
			return Response.status(Status.CREATED).location(getSelfLink(surelythesameuuid)).build();
		}
		catch( InvalidDataException ide )
		{
			return Response.status(Status.BAD_REQUEST).build();
		}
		catch( Throwable t )
		{
			LOGGER.error("Error creating group", t);
			throw t;
		}
	}

	@Override
	public Response editGroup(String uuid, GroupBean groupBean)
	{
		TLEGroup tleGroup = tleGroupService.get(uuid);
		if( tleGroup != null )
		{
			tleGroup.setUuid(groupBean.getId());
			tleGroup.setName(groupBean.getName());
			tleGroup.setUsers(groupBean.getUsers());
			final String parentId = groupBean.getParentId();
			if( parentId != null )
			{
				TLEGroup parentVal = tleGroupService.get(parentId);
				tleGroup.setParent(parentVal);
			}
			else
			{
				tleGroup.setParent(null);
			}
			tleGroupService.edit(tleGroup);
			return Response.ok().build();
		}
		else
		{
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Override
	public Response deleteGroup(String uuid)
	{
		return deleteGroup(uuid, true);
	}

	@DELETE
	@Path("/{uuid}")
	@ApiOperation("Delete a group")
	public Response deleteGroup(
		// @formatter:off
			@ApiParam(APIDOC_GROUPUUID)
				@PathParam("uuid") String uuid,
			@ApiParam(value = "also delete subgroups", allowableValues = "true,false", defaultValue = "true", required = false)
				@QueryParam("cascade")
				@DefaultValue("false")
				Boolean deleteSubgroups
			// @formatter:on
	)
	{
		boolean deleteSub = (deleteSubgroups == null ? false : deleteSubgroups);
		// ensure group exists, else throw 404
		TLEGroup tleGroup = tleGroupService.get(uuid);
		if( tleGroup == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		tleGroupService.delete(uuid, deleteSub);
		return Response.status(Status.NO_CONTENT).build();
	}

	private GroupBean apiGroupBeanFromTLEGroup(TLEGroup tleGroup, boolean includeUsers)
	{
		GroupBean newB = new GroupBean(tleGroup.getUuid(), tleGroup.getName());

		int numberOfUsers = Check.isEmpty(tleGroup.getUsers()) ? 0 : tleGroup.getUsers().size();
		// newB.setUserTotal(numberOfUsers);
		// List<ApiUserBean> userBeans = Lists.newArrayList();
		Set<String> userIds = Sets.newHashSet();
		if( includeUsers && numberOfUsers > 0 )
		{
			for( String userUuid : tleGroup.getUsers() )
			{
				// ApiUserBean userBean = new ApiUserBean();
				// userBean.setId(userUuid);
				// userBean.set("links", Collections.singletonMap("self",
				// getUserLink(userUuid)));
				// userBeans.add(userBean);
				userIds.add(userUuid);
			}
		}

		// if includeUsers is true, always add the user list - even if empty
		if( includeUsers )
		{
			newB.setUsers(userIds);
		}

		if( tleGroup.getParent() != null )
		{
			// all we want for the parent is the uuid and link. Ignore it's
			// name, users, grandparent etc

			// TODO: I like this, but EPS model is shit.
			// GroupBean parentBean = new
			// GroupBean(tleGroup.getParent().getUuid(), null);
			// parentBean.set("links", Collections.singletonMap("self",
			// getSelfLink(parentBean.getId())));
			newB.setParentId(tleGroup.getParent().getUuid());
		}

		Map<String, String> links = Collections.singletonMap("self", getSelfLink(newB.getId()).toString());
		newB.set("links", links);

		return newB;
	}

	private TLEGroup populateTLEGroup(GroupBean groupBean)
	{
		TLEGroup tleGroup = tleGroupService.createGroup(groupBean.getId(), groupBean.getName());
		tleGroup.setUsers(groupBean.getUsers());
		final String parentId = groupBean.getParentId();
		if( parentId != null )
		{
			tleGroup.setParent(tleGroupService.get(parentId));
		}
		return tleGroup;
	}

	private void ensurePriv()
	{
		final Set<String> stillThere = aclManager.filterNonGrantedPrivileges("EDIT_USER_MANAGEMENT");
		if( stillThere.isEmpty() )
		{
			throw new AccessDeniedException("EDIT_USER_MANAGEMENT not granted");
		}
	}

	private URI getSelfLink(String userUuid)
	{
		return urlLinkService.getMethodUriBuilder(EquellaGroupResource.class, "getGroup").build(userUuid);
	}
}
