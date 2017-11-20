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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.usermanagement.standard.ldap.LDAP.AndFilter;
import com.tle.core.usermanagement.standard.ldap.LDAP.Filter;
import com.tle.core.usermanagement.standard.ldap.LDAP.FullNameHitsCollector;
import com.tle.core.usermanagement.standard.ldap.LDAP.GroupBeanHitsCollector;
import com.tle.core.usermanagement.standard.ldap.LDAP.HitsCollector;
import com.tle.core.usermanagement.standard.ldap.LDAP.LDAPResult;
import com.tle.core.usermanagement.standard.ldap.LDAP.LdapResultHitsCollector;
import com.tle.core.usermanagement.standard.ldap.LDAP.OrFilter;
import com.tle.core.usermanagement.standard.ldap.LDAP.SingleFilter;
import com.tle.core.usermanagement.standard.ldap.LDAP.UserBeanHitsCollector;

@SuppressWarnings("nls")
public class MemberOfGroupSearch extends GroupSearch
{
	private static final Log LOGGER = LogFactory.getLog(MemberOfGroupSearch.class);

	private final String memberField;
	private final String memberOfField;
	private final String memberUserField;

	private final String[] memberUserAttributes;

	public MemberOfGroupSearch(String memberField, String memberOfField, String memberUserField, LDAP ldap)
	{
		super(ldap);

		this.memberField = memberField;
		this.memberOfField = memberOfField;
		this.memberUserField = memberUserField;

		memberUserAttributes = new String[]{memberUserField, memberOfField,};
	}

	@Override
	public GroupBean getParentGroupForGroup(DirContext ctx, String groupID)
	{
		SubgroupResultHitsCollector collector = new SubgroupResultHitsCollector();

		SubgroupResultHitsCollector.SubgroupResult result = null;
		if( !Check.isEmpty(ldap.getGroupIdField()) )
		{
			SingleFilter nv1 = new SingleFilter(LDAP.OBJECTCLASS, ldap.getGroupObject());
			SingleFilter nv2 = new SingleFilter(ldap.getGroupIdField(), groupID);
			result = ldap.searchFirstResultAllBases(ctx, new AndFilter(nv1, nv2), collector, true);
		}
		else
		{
			try
			{
				Name name = LDAP.parse(groupID);
				result = collector.new SubgroupResult(name, ctx.getAttributes(name, collector.getReturnAttributes()));
			}
			catch( NamingException e )
			{
				LOGGER.debug(e, e);
			}
		}

		if( result != null )
		{
			// Now get the parent group
			List<SubgroupResultHitsCollector.SubgroupResult> parentGroups = result.getParentGroups();
			if( !Check.isEmpty(parentGroups) )
			{
				result = parentGroups.get(0);
			}
			else
			{
				result = null;
			}
		}

		return result == null ? null : result.getGroupBean();
	}

	@Override
	public List<GroupBean> getGroupsContainingUser(DirContext ctx, String userID)
	{
		LDAPResult res = ldap.searchFirstResultAllBases(ctx, ldap.getUserIDFilter(userID), new LdapResultHitsCollector(
			memberUserAttributes), true);
		if( res == null )
		{
			return Collections.emptyList();
		}

		if( !Check.isEmpty(memberField) )
		{
			// Get groups is an immediate member of
			List<SubgroupResultHitsCollector.SubgroupResult> immediateGroups = ldap.searchAllBases(ctx,
				getMemberFilter(getUserUid(res)), new SubgroupResultHitsCollector(), true);

			// Add known groups, then collect all parents for all above results
			Set<SubgroupResultHitsCollector.SubgroupResult> allGroups = new HashSet<SubgroupResultHitsCollector.SubgroupResult>(
				immediateGroups);
			for( SubgroupResultHitsCollector.SubgroupResult sgr : immediateGroups )
			{
				collectAllParentGroups(allGroups, ctx, sgr);
			}

			// Convert to GroupBeans and return
			List<GroupBean> groupBeans = new ArrayList<GroupBean>(allGroups.size());
			for( SubgroupResultHitsCollector.SubgroupResult sgr : allGroups )
			{
				GroupBean gb = sgr.getGroupBean();
				if( gb != null )
				{
					groupBeans.add(gb);
				}
			}
			return groupBeans;
		}
		// Not sure about this case. I think it's only for Mac OSX Server
		Collection<Name> ldapgroupnames = getLDAPGroupNames(ctx, res.getAttributes());
		return getGroupsFromNames(ctx, ldapgroupnames);
	}

	private String getUserUid(LDAPResult res)
	{
		if( Check.isEmpty(memberUserField) )
		{
			return res.getFullName().toString();
		}
		try
		{
			return (String) res.getAttributes().get(memberUserField).get();
		}
		catch( NamingException e )
		{
			throw new RuntimeException("Error getting memberUserField", e);
		}
	}

	private String getGroupName(LDAPResult res)
	{
		if( Check.isEmpty(memberField) )
		{
			return res.getFullName().toString();
		}
		try
		{
			return (String) res.getAttributes().get(ldap.getGroupNameField()).get();
		}
		catch( NamingException e )
		{
			throw new RuntimeException("Error getting groupName", e);
		}
	}

	@Override
	public List<UserBean> getUsersInGroup(DirContext ctx, String query, String groupID, boolean recurse)
	{
		if( !Check.isEmpty(groupID) )
		{
			LDAPResult res = ldap.getGroupResult(ctx, groupID,
				Check.isEmpty(memberOfField) ? ldap.getExtGroupAttributes() : LDAP.ATTRIBUTES_NONE);
			if( res != null )
			{
				if( !Check.isEmpty(memberOfField) )
				{
					Filter filter;
					if( recurse )
					{
						Set<String> groups = new HashSet<String>();
						collectAllSubGroupFullNames(groups, ctx, res.getFullName());
						filter = getMemberOfFilter(query, groups);
					}
					else
					{
						filter = getMemberOfFilter(query, res.getFullName().toString());
					}

					return ldap.searchAllBases(ctx, filter, new UserBeanHitsCollector(), true);
				}

				return getUserSet(ctx, query, res.getAttributes());

			}
		}
		return Collections.emptyList();
	}

	private void collectAllSubGroupFullNames(Set<String> results, DirContext ctx, Name parent)
	{
		final String fullname = parent.toString();
		if( results.add(fullname) )
		{
			for( Name child : ldap.searchAllBases(ctx, getSubgroupsByMemberOfFilter(fullname),
				new FullNameHitsCollector(), true) )
			{
				collectAllSubGroupFullNames(results, ctx, child);
			}
		}
	}

	private void collectAllSubGroups(Set<String> results, DirContext ctx, Attributes parent)
	{
		Attribute attribute = parent.get(memberField);

		try
		{
			NamingEnumeration<?> enumeration = attribute.getAll();
			int limit = ldap.config.getSearchLimit();

			while( enumeration != null && enumeration.hasMore() && (limit == 0 || results.size() < limit) )
			{
				String groupName = (String) enumeration.next();

				LDAPResult result = ldap.searchFirstResultAllBases(ctx, ldap.getGroupNameFilter(groupName),
					new LdapResultHitsCollector(ldap.getExtGroupAttributes()), true);

				if( result != null && results.add(getGroupName(result)) )
				{
					collectAllSubGroups(results, ctx, result.getAttributes());
				}
			}
		}
		catch( Exception e )
		{
			LOGGER.error("", e);
		}
	}

	private void collectAllParentGroups(Set<SubgroupResultHitsCollector.SubgroupResult> results, DirContext ctx,
		SubgroupResultHitsCollector.SubgroupResult sgr)
	{
		List<SubgroupResultHitsCollector.SubgroupResult> parents = sgr.getParentGroups();
		if( parents != null )
		{
			for( SubgroupResultHitsCollector.SubgroupResult parent : parents )
			{
				if( parent != null && results.add(parent) )
				{
					collectAllParentGroups(results, ctx, parent);
				}
			}
		}
	}

	private Collection<Name> getLDAPGroupNames(DirContext ctx, Attributes useratt)
	{
		Set<Name> foundGroups = new HashSet<Name>();
		if( !Check.isEmpty(memberOfField) )
		{
			Attribute attribute = useratt.get(memberOfField);
			try
			{
				NameParser parser = ctx.getNameParser(""); //$NON-NLS-1$
				if( attribute != null )
				{
					NamingEnumeration<?> enumeration = attribute.getAll();
					while( enumeration != null && enumeration.hasMore() )
					{
						String role = (String) enumeration.next();
						Name compound = parser.parse(role);
						foundGroups.add(compound);
					}
				}
			}
			catch( NamingException e )
			{
				throw new RuntimeException("Couldn't get memberField", e);
			}
		}
		return foundGroups;
	}

	private List<UserBean> getUserSet(DirContext ctx, String query, Attributes groupatt)
	{
		query = ldap.checkQuery(query);

		Attribute attribute = groupatt.get(memberField);
		if( attribute == null )
		{
			return Collections.emptyList();
		}

		List<UserBean> foundUsers = new ArrayList<UserBean>();
		try
		{
			NamingEnumeration<?> enumeration = attribute.getAll();
			int limit = ldap.config.getSearchLimit();
			while( enumeration != null && enumeration.hasMore() && (limit == 0 || foundUsers.size() < limit) )
			{
				String userLoc = (String) enumeration.next();
				if( Check.isEmpty(memberUserField) )
				{
					try
					{
						Name username = LDAP.parse(userLoc);

						UserBean uattr = null;
						if( ldap.isSearchAllQuery(query) )
						{
							uattr = ldap.getUserBean(ctx, username);
						}
						else
						{
							// There is probably an easier way to do this...
							Filter f = ldap.getUserSearchFilter(query);
							uattr = ldap.searchFirstResult(ctx, username, f, new UserBeanHitsCollector(), true);
						}

						if( uattr != null )
						{
							foundUsers.add(uattr);
						}
					}
					catch( NamingException e )
					{
						LOGGER.warn("Error retrieving username:" + userLoc);
					}
				}
				else
				{
					Filter filter = new AndFilter(ldap.getUserSearchFilter(query), new SingleFilter(memberUserField,
						userLoc));
					UserBean bean = ldap.searchFirstResultAllBases(ctx, filter, new UserBeanHitsCollector(), true);
					if( bean != null )
					{
						foundUsers.add(bean);
					}
				}
			}
		}
		catch( Exception e )
		{
			LOGGER.error("", e);
			return null;
		}
		return foundUsers;
	}

	private Filter getMemberFilter(String member)
	{
		return new AndFilter(new SingleFilter(LDAP.OBJECTCLASS, ldap.getGroupObject()), new SingleFilter(memberField,
			member));
	}

	private Filter getSubgroupsByMemberOfFilter(String parentGroup)
	{
		return new AndFilter(new SingleFilter(LDAP.OBJECTCLASS, ldap.getGroupObject()), new SingleFilter(memberOfField,
			parentGroup));
	}

	private Filter getMemberOfFilter(String query, String group)
	{
		return new AndFilter(ldap.getUserSearchFilter(query), new SingleFilter(memberOfField, group));
	}

	private Filter getMemberOfFilter(String query, Collection<String> groups)
	{
		OrFilter groupsFilter = new OrFilter();
		for( String group : groups )
		{
			groupsFilter.addFilter(new SingleFilter(memberOfField, group));
		}
		return new AndFilter(ldap.getUserSearchFilter(query), groupsFilter);
	}

	private Filter getMemberOfGroupFilter(String query, String group)
	{
		return new AndFilter(ldap.getGroupSearchFilter(query), new SingleFilter(memberOfField, group));
	}

	private Filter getMemberOfGroupFilter(String query, Collection<String> groups)
	{
		OrFilter groupsFilter = new OrFilter();
		for( String group : groups )
		{
			groupsFilter.addFilter(new SingleFilter(memberOfField, group));
		}
		return new AndFilter(ldap.getGroupSearchFilter(query), groupsFilter);
	}

	private Filter getMemberGroupNameFilter(String query, Collection<String> groups)
	{
		OrFilter groupsFilter = new OrFilter();
		for( String group : groups )
		{
			groupsFilter.addFilter(new SingleFilter(ldap.getGroupNameField(), group));
		}
		return new AndFilter(ldap.getGroupSearchFilter(query), groupsFilter);
	}

	private class SubgroupResultHitsCollector extends HitsCollector<SubgroupResultHitsCollector.SubgroupResult>
	{
		private String[] returnAttributes;

		@Override
		protected void setup(DirContext ctx, LDAP ldap)
		{
			super.setup(ctx, ldap);

			returnAttributes = new String[]{ldap.getGroupIdField(), ldap.getGroupNameField(), memberOfField,};
		}

		@Override
		public void addResult(SearchResult sr, Name base) throws NamingException
		{
			results.add(new SubgroupResult(LDAP.parse(sr.getNameInNamespace()), sr.getAttributes()));
		}

		@Override
		public String[] getReturnAttributes()
		{
			return returnAttributes;
		}

		public class SubgroupResult
		{
			private final Name name;
			private final Attributes attributes;

			public SubgroupResult(Name name, Attributes attributes)
			{
				this.name = name;
				this.attributes = attributes;
			}

			public GroupBean getGroupBean()
			{
				return ldap.getGroupBeanFromResult(name, attributes);
			}

			public List<SubgroupResult> getParentGroups()
			{
				if( !Check.isEmpty(memberOfField) )
				{
					Attribute attribute = attributes.get(memberOfField);
					if( attribute != null )
					{
						try
						{
							NamingEnumeration<?> atts = attribute.getAll();
							List<SubgroupResult> results = Lists.newArrayList();
							while( atts.hasMoreElements() )
							{
								String n = atts.nextElement().toString();
								Name parentGroupName = LDAP.parse(n);
								results.add(new SubgroupResult(parentGroupName, ldap.getAttributes(ctx,
									parentGroupName, returnAttributes)));
							}
							return results;
						}
						catch( NamingException e )
						{
							throw new RuntimeException(e);
						}
					}
				}
				else if( !Check.isEmpty(memberField) )
				{
					return ldap.searchAllBases(ctx, getMemberFilter(name.toString()),
						new SubgroupResultHitsCollector(), true);
				}
				return null;
			}

			@Override
			public int hashCode()
			{
				return name.hashCode();
			}

			@Override
			public boolean equals(Object obj)
			{
				if( !(obj instanceof SubgroupResult) )
				{
					return false;
				}
				return this == obj || ((SubgroupResult) obj).name.equals(name);

			}
		}
	}

	@Override
	public List<GroupBean> searchGroupsInGroup(DirContext ctx, String query, String groupID, boolean recurse)
	{
		if( !Check.isEmpty(groupID) )
		{
			// Get top group
			LDAPResult res = ldap.getGroupResult(ctx, groupID,
				Check.isEmpty(memberOfField) ? ldap.getExtGroupAttributes() : LDAP.ATTRIBUTES_NONE);
			if( res != null )
			{
				Filter filter;
				if( !Check.isEmpty(memberOfField) )
				{
					if( recurse )
					{
						Set<String> groups = new HashSet<String>();
						collectAllSubGroupFullNames(groups, ctx, res.getFullName());
						filter = getMemberOfGroupFilter(query, groups);
					}
					else
					{
						filter = getMemberOfGroupFilter(query, res.getFullName().toString());
					}

					return ldap.searchAllBases(ctx, filter, new GroupBeanHitsCollector(), true);
				}

				Set<String> groups = new HashSet<String>();
				collectAllSubGroups(groups, ctx, res.getAttributes());

				filter = getMemberGroupNameFilter(query, groups);

				return ldap.searchAllBases(ctx, filter, new GroupBeanHitsCollector(), true);
			}
		}
		return Collections.emptyList();
	}
}
