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

package com.tle.core.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.valuebean.DefaultGroupBean;
import com.dytech.edge.common.valuebean.DefaultRoleBean;
import com.dytech.edge.common.valuebean.DefaultUserBean;
import com.dytech.edge.common.valuebean.GroupBean;
import com.dytech.edge.common.valuebean.RoleBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.reporting.IResultSetExt;
import com.tle.reporting.MetadataBean;

@Bind
@Singleton
@SuppressWarnings("nls")
public class UmpQueryDelegate extends SimpleTypeQuery
{
	@Inject
	private UserService userService;

	enum UmpQueryType
	{
		USERS_IN_GROUP("ug"), USER("u"), USER_SEARCH("su"), GROUP("g"), GROUP_SEARCH("sg"), GROUPS_FOR_USER("gfu"),
		ROLE("r");

		private final String prefix;
		private static Map<String, UmpQueryType> prefixMap;

		UmpQueryType(String prefix)
		{
			this.prefix = prefix;
			addToPrefix(this);
		}

		private void addToPrefix(UmpQueryType type)
		{
			if( prefixMap == null )
			{
				prefixMap = new HashMap<String, UmpQueryType>();
			}
			prefixMap.put(prefix, this);
		}

		public String getPrefix()
		{
			return prefix;
		}

		public static UmpQueryType getType(String prefix)
		{
			return prefixMap.get(prefix);
		}
	}

	@Override
	public Map<String, ?> getDatasourceMetadata()
	{
		throw new RuntimeException("Functionality not complete");
	}

	@Override
	public IResultSetExt executeQuery(String query, List<Object> params, int maxRows)
	{
		MetadataBean bean = new MetadataBean();
		String[] queryStrings = getQueryStrings(query, params);
		UmpQueryType type = UmpQueryType.getType(query.substring(0, query.indexOf(':')));
		query = queryStrings[0];
		switch( type )
		{
			case USER_SEARCH:
				List<UserBean> users = userService.searchUsers(query);
				return doUserList(users, bean);
			case USERS_IN_GROUP:
				users = userService.getUsersInGroup(query, false);
				return doUserList(users, bean);
			case USER:
				UserBean userBean = userService.getInformationForUser(query);
				if( userBean == null )
				{
					userBean = new DefaultUserBean(query, "{" + query + "}", "", "", "");
				}
				return doUserList(Collections.singletonList(userBean), bean);
			case GROUP:
				GroupBean groupBean = userService.getInformationForGroup(query);
				if( groupBean == null )
				{
					groupBean = new DefaultGroupBean(query, "{" + query + "}");
				}
				return doGroupList(Collections.singletonList(groupBean), bean);
			case ROLE:
				RoleBean roleBean = userService.getInformationForRole(query);
				if( roleBean == null )
				{
					roleBean = new DefaultRoleBean(query, "{" + query + "}");
				}
				return doRoleList(Collections.singletonList(roleBean), bean);
			case GROUP_SEARCH:
				List<GroupBean> groups = userService.searchGroups(query);
				return doGroupList(groups, bean);
			case GROUPS_FOR_USER:
				groups = userService.getGroupsContainingUser(query);
				return doGroupList(groups, bean);
		}
		throw new UnsupportedOperationException("Unknown query type:" + type);
	}

	private IResultSetExt doGroupList(List<GroupBean> groups, MetadataBean bean)
	{
		List<Object[]> retResults = new ArrayList<Object[]>();
		for( GroupBean group : groups )
		{
			if( group != null )
			{
				retResults.add(new Object[]{group.getUniqueID(), group.getName()});
			}
		}
		addColumn("id", TYPE_STRING, bean);
		addColumn("name", TYPE_STRING, bean);
		return new SimpleResultSet(retResults, bean);
	}

	private IResultSetExt doRoleList(List<RoleBean> roles, MetadataBean bean)
	{
		List<Object[]> retResults = new ArrayList<Object[]>();
		for( RoleBean role : roles )
		{
			if( role != null )
			{
				retResults.add(new Object[]{role.getUniqueID(), role.getName()});
			}
		}
		addColumn("id", TYPE_STRING, bean);
		addColumn("name", TYPE_STRING, bean);
		return new SimpleResultSet(retResults, bean);
	}

	private IResultSetExt doUserList(List<UserBean> users, MetadataBean bean)
	{
		List<Object[]> retResults = new ArrayList<Object[]>();
		for( UserBean userBean : users )
		{
			if( userBean != null )
			{
				retResults.add(new Object[]{userBean.getUniqueID(), userBean.getUsername(), userBean.getFirstName(),
						userBean.getLastName(), userBean.getEmailAddress()});
			}
		}
		addColumn("id", TYPE_STRING, bean);
		addColumn("username", TYPE_STRING, bean);
		addColumn("firstname", TYPE_STRING, bean);
		addColumn("lastname", TYPE_STRING, bean);
		addColumn("email", TYPE_STRING, bean);
		return new SimpleResultSet(retResults, bean);
	}
}
