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

package com.tle.core.usermanagement.standard.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.common.Check;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.usermanagement.standard.ldap.LDAP.GroupBeanHitsCollector;
import com.tle.core.usermanagement.standard.ldap.LDAP.LDAPResult;
import com.tle.core.usermanagement.standard.ldap.LDAP.LdapResultHitsCollector;
import com.tle.core.usermanagement.standard.ldap.LDAP.UserBeanHitsCollector;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public class DirectoryGroupSearch extends GroupSearch
{
	private static final Log LOGGER = LogFactory.getLog(DirectoryGroupSearch.class);

	public DirectoryGroupSearch(LDAP ldap)
	{
		super(ldap);
	}

	@Override
	public List<GroupBean> getGroupsContainingUser(DirContext ctx, String userID)
	{
		LDAPResult sr = ldap.searchFirstResultAllBases(ctx, ldap.getUserIDFilter(userID), new LdapResultHitsCollector(
			LDAP.ATTRIBUTES_NONE), true);
		if( sr == null )
		{
			return Collections.emptyList();
		}

		List<Name> dirs = new ArrayList<Name>();
		try
		{
			Name groupName = sr.getFullName();
			Name base = sr.getBaseName();
			int sz;
			// Some of these might not be groups, but so what...
			while( (sz = groupName.size()) > base.size() + 1 )
			{
				groupName.remove(sz - 1);
				dirs.add((Name) groupName.clone());
			}
		}
		catch( NamingException e )
		{
			LOGGER.error(e, e);
			return new ArrayList<GroupBean>();
		}
		return getGroupsFromNames(ctx, dirs);
	}

	@Override
	public List<UserBean> getUsersInGroup(DirContext ctx, String query, String groupID, boolean recurse)
	{
		if( groupID.length() == 0 )
		{
			return Collections.emptyList();
		}

		Name groupName = getFullGroupName(ctx, groupID);
		return ldap.search(ctx, groupName, ldap.getUserSearchFilter(query), new UserBeanHitsCollector(), recurse);
	}

	@Override
	public List<GroupBean> searchGroupsInGroup(DirContext ctx, String query, String groupID, boolean recurse)
	{
		if( groupID.length() == 0 )
		{
			return Collections.emptyList();
		}

		Name groupName = getFullGroupName(ctx, groupID);
		return ldap.search(ctx, groupName, ldap.getGroupSearchFilter(query), new GroupBeanHitsCollector(), recurse);
	}

	/**
	 * @param groupID
	 */
	private Name getFullGroupName(DirContext ctx, String groupID)
	{
		if( Check.isEmpty(groupID) )
		{
			try
			{
				return LDAP.parse(groupID);
			}
			catch( InvalidNameException e )
			{
				LOGGER.debug(e, e);
				return null;
			}
		}
		LDAPResult groupRes = ldap.getGroupResult(ctx, groupID, ldap.getGroupAttributes());
		if( groupRes == null )
		{
			return null;
		}

		try
		{
			return groupRes.getFullName();
		}
		catch( Exception e )
		{
			LOGGER.error("Couldn't create full name");
			return null;
		}
	}

	@Override
	public GroupBean getParentGroupForGroup(DirContext ctx, String groupID)
	{
		LDAPResult groupRes = ldap.getGroupResult(ctx, groupID, ldap.getGroupAttributes());
		if( groupRes == null )
		{
			return null;
		}

		try
		{
			Name groupName = groupRes.getFullName();
			int sz = groupName.size();
			if( sz > 1 )
			{
				groupName.remove(sz - 1);
				Attributes attributes = ctx.getAttributes(groupName);
				Attribute attribute = attributes.get(LDAP.OBJECTCLASS);
				if( attribute != null && attribute.contains(ldap.getGroupObject()) )
				{
					return ldap.getGroupBeanFromResult(groupRes);
				}
			}
		}
		catch( NamingException e )
		{
			LOGGER.error(e, e);
		}
		return null;
	}
}
