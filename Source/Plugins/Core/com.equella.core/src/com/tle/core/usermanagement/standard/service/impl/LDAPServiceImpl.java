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

package com.tle.core.usermanagement.standard.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.tle.beans.usermanagement.standard.LDAPSettings;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.usermanagement.standard.ldap.LDAP;
import com.tle.core.usermanagement.standard.ldap.LDAP.FullNameHitsCollector;
import com.tle.core.usermanagement.standard.ldap.LDAP.InContext;
import com.tle.core.usermanagement.standard.ldap.LDAP.UserBeanHitsCollector;
import com.tle.core.usermanagement.standard.service.LDAPService;

@Bind(LDAPService.class)
@Singleton
public class LDAPServiceImpl implements LDAPService
{
	@Inject
	private EncryptionService encryptionService;

	@Override
	public List<? extends Attribute> getAttributes(LDAPSettings settings, final String base, final String[] attributes)
	{
		return new LDAP(settings, encryptionService).doAsAdmin(new InContext<List<? extends Attribute>>()
		{
			@Override
			public List<? extends Attribute> execute(DirContext ctx) throws NamingException
			{
				return Collections.list(ctx.getAttributes(LDAP.parse(base), attributes).getAll());
			}
		});
	}

	@Override
	public List<SearchResult> search(LDAPSettings settings, final String base, final String filter,
		final SearchControls ctls)
	{
		return new LDAP(settings, encryptionService).doAsAdmin(new InContext<List<SearchResult>>()
		{
			@Override
			public List<SearchResult> execute(DirContext ctx) throws NamingException
			{
				return Collections.list(ctx.search(base, filter, ctls));
			}
		});
	}

	@Override
	public List<Name> getBases(LDAPSettings settings)
	{
		return new LDAP(settings, encryptionService).getBases();
	}

	@Override
	public List<String> getDNs(LDAPSettings settings)
	{
		LDAP ldap = new LDAP(settings, encryptionService);
		return ldap.getDNs();
	}

	@Override
	public String searchAuthenticate(LDAP ldap, String username, String password)
	{
		return ldap.searchAuthenticate(username, password);
	}

	@Override
	public String getTokenFromUsername(final LDAP ldap, final String username)
	{
		return ldap.doAsAdmin(new InContext<String>()
		{
			@Override
			public String execute(DirContext ctx)
			{

				Name results = ldap.searchFirstResultAllBases(ctx, ldap.getUsernameFilter(username),
					new FullNameHitsCollector(), true);
				if( results != null )
				{
					return results.toString();
				}
				return null;
			}
		});
	}

	@Override
	public UserBean getUserBean(final LDAP ldap, final String userID)
	{
		return ldap.doAsAdmin(new InContext<UserBean>()
		{
			@Override
			public UserBean execute(DirContext ctx) throws NamingException
			{
				return ldap.searchFirstResultAllBases(ctx, ldap.getUserIDFilter(userID), new UserBeanHitsCollector(),
					true);
			}
		});
	}

	@Override
	public Collection<GroupBean> getGroupsContainingUser(final LDAP ldap, final String userID)
	{
		return ldap.doAsAdmin(new InContext<List<GroupBean>>()
		{
			@Override
			public List<GroupBean> execute(DirContext ctx)
			{
				return ldap.getGroupSearch().getGroupsContainingUser(ctx, userID);
			}
		});
	}

	@Override
	public Collection<UserBean> getUsersInGroup(final LDAP ldap, final String query, final String parentGroupID,
		final boolean recursive)
	{
		return ldap.doAsAdmin(new InContext<List<UserBean>>()
		{
			@Override
			public List<UserBean> execute(DirContext ctx)
			{
				return ldap.getGroupSearch().getUsersInGroup(ctx, query, parentGroupID, recursive);
			}
		});
	}

	@Override
	public Collection<UserBean> searchUsers(final LDAP ldap, final String query)
	{
		return ldap.doAsAdmin(new InContext<Collection<UserBean>>()
		{
			@Override
			public Collection<UserBean> execute(DirContext ctx)
			{
				return ldap.searchAllBases(ctx, ldap.getUserSearchFilter(query), new UserBeanHitsCollector(), true);
			}
		});
	}

	@Override
	public GroupBean getParentGroupForGroup(final LDAP ldap, final String groupID)
	{

		return ldap.doAsAdmin(new InContext<GroupBean>()
		{
			@Override
			public GroupBean execute(DirContext ctx) throws NamingException
			{
				return ldap.getGroupSearch().getParentGroupForGroup(ctx, groupID);
			}
		});
	}

	@Override
	public GroupBean getGroupBean(final LDAP ldap, final String groupID)
	{
		return ldap.doAsAdmin(new InContext<GroupBean>()
		{
			@Override
			public GroupBean execute(DirContext ctx) throws NamingException
			{
				return ldap.getGroupBeanFromResult(ldap.getGroupResult(ctx, groupID, ldap.getGroupAttributes()));
			}
		});
	}

	@Override
	public Collection<GroupBean> searchGroups(final LDAP ldap, final String query)
	{
		return ldap.doAsAdmin(new InContext<List<GroupBean>>()
		{
			@Override
			public List<GroupBean> execute(DirContext ctx)
			{
				return ldap.getGroupSearch().search(ctx, ldap.checkQuery(query));
			}
		});
	}

	@Override
	public Collection<GroupBean> searchGroups(final LDAP ldap, final String query, final String parentGroupId)
	{
		return ldap.doAsAdmin(new InContext<List<GroupBean>>()
		{
			@Override
			public List<GroupBean> execute(DirContext ctx)
			{
				return ldap.getGroupSearch().searchGroupsInGroup(ctx, ldap.checkQuery(query), parentGroupId, true);
			}
		});
	}

	@Override
	public UserBean resolveUserFromToken(final LDAP ldap, final String token)
	{
		return ldap.doAsAdmin(new InContext<UserBean>()
		{
			@Override
			public UserBean execute(DirContext ctx) throws NamingException
			{
				return ldap.getUserBean(ctx, LDAP.parse(token));
			}
		});
	}
}
