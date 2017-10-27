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

package com.tle.core.usermanagement.standard;

import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.dytech.devlib.Md5;
import com.google.common.collect.Maps;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.usermanagement.standard.ReplicatedConfiguration;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.usermanagement.user.valuebean.DefaultGroupBean;
import com.tle.common.usermanagement.user.valuebean.DefaultRoleBean;
import com.tle.common.usermanagement.user.valuebean.DefaultUserBean;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.DefaultUserState;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.exceptions.BadCredentialsException;
import com.tle.exceptions.DisabledException;
import com.tle.plugins.ump.AbstractUserDirectory;
import com.tle.plugins.ump.UserDirectoryUtils;
import com.zaxxer.hikari.HikariDataSource;

@Bind
public class ReplicatedUserPlugin extends AbstractUserDirectory
{
	private static Logger LOGGER = Logger.getLogger(ReplicatedUserPlugin.class);

	// need to chunk the IN statement into manageable chunks (there is a 1000
	// maximum limit to the size of IN statements on Oracle)
	private static final int MAX_IN_PARAMETERS = 500;

	private ReplicatedConfiguration config;
	private JdbcTemplate jdbcTemplate;
	private HikariDataSource source;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		config = (ReplicatedConfiguration) settings;

		source = new HikariDataSource();
		source.setDriverClassName(config.getDriver());
		source.setUsername(config.getUsername());
		source.setPassword(config.getPassword());
		source.setJdbcUrl(config.getUrl());
		jdbcTemplate = new JdbcTemplate(source);

		return false;
	}

	@Override
	public void close() throws Exception
	{
		source.close();
	}

	@Override
	public ModifiableUserState authenticateUser(final String username, final String password)
	{
		final String sql = config.getAuthenticate();
		if( Check.isEmpty(sql) )
		{
			return null;
		}

		String innerPass = password;
		String digest = config.getDigest();
		boolean isNotPlainText = digest.length() > 0 && !digest.equals(ReplicatedConfiguration.DIGEST_PLAINTEXT);
		try
		{
			if( isNotPlainText )
			{
				byte[] mac = MessageDigest.getInstance(digest).digest(innerPass.getBytes());
				innerPass = Md5.stringify(mac);
			}
		}
		catch( Exception e )
		{
			throw new BadCredentialsException("Username or password incorrect");
		}

		UserInfo user = authenticate(username);
		String pass = user.getPassword();
		if( pass == null || (isNotPlainText ? pass.equalsIgnoreCase(innerPass) : pass.equals(innerPass)) )
		{
			return getUserState(user.getId());
		}

		return null;
	}

	@Override
	public ModifiableUserState authenticateUserFromUsername(final String username, String privateData)
	{
		String sql = config.getAuthenticate();
		if( Check.isEmpty(sql) )
		{
			return null;
		}

		return getUserState(authenticate(username).getId());
	}

	@Override
	public void initUserState(ModifiableUserState auth)
	{
		final String userId = auth.getUserBean().getUniqueID();

		Set<String> roles = auth.getUsersRoles();

		Pair<ChainResult, Collection<RoleBean>> rfu = getRolesForUser(userId);
		if( rfu != null )
		{
			for( RoleBean b : rfu.getSecond() )
			{
				roles.add(b.getUniqueID());
			}
		}

		Set<String> groups = auth.getUsersGroups();
		List<String> groupsForUser = getGroupIdsContainingUsers(userId);
		if( groupsForUser != null )
		{
			for( String b : groupsForUser )
			{
				groups.add(b);
			}
		}
	}

	@Override
	public UserBean getInformationForUser(final String userId)
	{
		return UserDirectoryUtils.getSingleUserInfoFromMultipleInfo(this, userId);
	}

	@Override
	public Map<String, UserBean> getInformationForUsers(final Collection<String> userIds)
	{
		String sql = config.getUserInfo();
		if( Check.isEmpty(sql) )
		{
			return null;
		}

		final Map<String, UserBean> users = new HashMap<String, UserBean>();
		handleIn(sql, userIds, new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet set) throws SQLException
			{
				String id = set.getString(1);
				String username = set.getString(2);
				String fname = set.getString(3);
				String lname = set.getString(4);
				String email = set.getString(5);
				DefaultUserBean user = new DefaultUserBean(id, username, fname, lname, email);
				users.put(id, user);
			}
		});
		return users;
	}

	@Override
	public Pair<ChainResult, Collection<GroupBean>> getGroupsContainingUser(final String userID)
	{
		List<String> gicu = getGroupIdsContainingUsers(userID);
		if( Check.isEmpty(gicu) )
		{
			return null;
		}
		return new Pair<ChainResult, Collection<GroupBean>>(ChainResult.CONTINUE, getGroupInfo(gicu).values());
	}

	private List<String> getGroupIdsContainingUsers(final String userId)
	{
		String sql = config.getGroupsContainingUser();
		if( Check.isEmpty(sql) )
		{
			return null;
		}
		return getIds(sql, userId);
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> getUsersForGroup(String groupId, boolean recursive)
	{
		if( recursive )
		{
			String sql = config.getUsersInGroupRecursive();
			if( !Check.isEmpty(sql) )
			{
				return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, getInformationForUsers(
					getIds(sql, groupId)).values());

			}
			// else fallback to non-recursive query below
		}

		String sql = config.getUsersInGroup();
		if( !Check.isEmpty(sql) )
		{
			return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, getInformationForUsers(
				getIds(sql, groupId)).values());
		}

		return null;
	}

	@Override
	public Pair<ChainResult, Collection<RoleBean>> getRolesForUser(final String userID)
	{
		String sql = config.getUserRoles();
		if( Check.isEmpty(sql) )
		{
			return null;
		}

		final Collection<RoleBean> roles = new ArrayList<RoleBean>();
		executeSql(sql, getParamValues(sql, userID), new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet set) throws SQLException
			{
				String id = set.getString(1);
				String name = set.getString(2);
				roles.add(new DefaultRoleBean(id, name));
			}
		});
		return new Pair<ChainResult, Collection<RoleBean>>(ChainResult.CONTINUE, roles);
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(String query)
	{
		String sql = config.getSearchUsers();
		if( Check.isEmpty(sql) )
		{
			return null;
		}

		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, getChain().getInformationForUsers(
			performSearch(sql, query.replace('*', '%'))).values());
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(final String query, final String parentGroupID,
		final boolean recursive)
	{
		if( Check.isEmpty(parentGroupID) )
		{
			return searchUsers(query);
		}

		final String fixedQuery = query.replace('*', '%');
		final Collection<String> userIds;
		if( recursive )
		{
			userIds = searchUsersInGroupRecursive(fixedQuery, parentGroupID);
		}
		else
		{
			userIds = searchUsersInGroup(fixedQuery, parentGroupID);
		}

		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, getChain().getInformationForUsers(
			userIds).values());
	}

	private List<String> searchUsersInGroupRecursive(String query, String group)
	{
		final String sql = config.getSearchUsersInGroupRecursive();
		if( !Check.isEmpty(sql) )
		{
			return getIds(sql, getParamValues(sql, group, query));
		}

		final String nonRecurseSql = config.getSearchUsersInGroup();
		if( !Check.isEmpty(nonRecurseSql) )
		{
			return searchUsersInGroup(query, group);
		}

		// fall back to old (broken) behaviour
		String oldSql = config.getUsersInGroupRecursive();
		if( !Check.isEmpty(oldSql) )
		{
			return getIds(oldSql, group);
		}

		return fallbackSearchUsersInGroup(group);
	}

	private List<String> searchUsersInGroup(String query, String group)
	{
		final String sql = config.getSearchUsersInGroup();
		if( !Check.isEmpty(sql) )
		{
			return getIds(sql, getParamValues(sql, group, query));
		}

		// fall back to old (broken) behaviour
		return fallbackSearchUsersInGroup(group);
	}

	/**
	 * This is the final fall-back in case all the other "search groups" queries
	 * are empty. It just gets all the users in the group. It is not correct,
	 * just a last resort.
	 */
	private List<String> fallbackSearchUsersInGroup(String group)
	{
		String sql = config.getUsersInGroup();
		if( Check.isEmpty(sql) )
		{
			return new ArrayList<String>();
		}
		return getIds(sql, group);
	}

	@Override
	public GroupBean getInformationForGroup(final String groupId)
	{
		return UserDirectoryUtils.getSingleGroupInfoFromMultipleInfo(this, groupId);
	}

	@Override
	public Map<String, GroupBean> getInformationForGroups(final Collection<String> groupIds)
	{
		return getGroupInfo(groupIds);
	}

	@Override
	public GroupBean getParentGroupForGroup(final String groupID)
	{
		String sql = config.getParentGroup();
		if( Check.isEmpty(sql) )
		{
			return null;
		}

		final UserInfo groupInfo = new UserInfo();
		final Object[] ids = getParamValues(sql, groupID);
		executeSql(sql, ids, new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet set) throws SQLException
			{
				String groupId = set.getString(1);
				String groupName = set.getString(2);
				groupInfo.setId(groupId);
				groupInfo.setPassword(groupName);
			}
		});

		if( groupInfo.getId() == null )
		{
			return null;
		}

		return new DefaultGroupBean(groupInfo.getId(), groupInfo.getPassword());
	}

	@Override
	public Collection<RoleBean> searchRoles(final String query)
	{
		String sql = config.getSearchRoles();
		if( Check.isEmpty(sql) )
		{
			return null;
		}

		final Collection<RoleBean> roles = new ArrayList<RoleBean>();
		executeSql(sql, getParamValues(sql, query.replace('*', '%')), new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet set) throws SQLException
			{
				String id = set.getString(1);
				String name = set.getString(2);
				roles.add(new DefaultRoleBean(id, name));
			}
		});
		return roles;
	}

	@Override
	public RoleBean getInformationForRole(final String roleId)
	{
		return UserDirectoryUtils.getSingleRoleInfoFromMultipleInfo(this, roleId);
	}

	@Override
	public Map<String, RoleBean> getInformationForRoles(final Collection<String> roleIds)
	{
		String sql = config.getRoleInfo();
		if( Check.isEmpty(sql) )
		{
			return null;
		}

		final Map<String, RoleBean> roles = Maps.newHashMapWithExpectedSize(roleIds.size());
		handleIn(sql, roleIds, new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet set) throws SQLException
			{
				String id = set.getString(1);
				String name = set.getString(2);
				roles.put(id, new DefaultRoleBean(id, name));
			}
		});
		return roles;
	}

	private UserInfo authenticate(String username)
	{
		String sql = config.getAuthenticate();

		final UserInfo user = new UserInfo();
		executeSql(sql, getParamValues(sql, username), new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet set) throws SQLException
			{
				boolean suspended = set.getInt(3) > 0;
				if( suspended )
				{
					throw new DisabledException("User suspended");
				}
				String id = set.getString(1);
				String password = set.getString(2);

				user.setId(id);
				user.setPassword(password);
			}
		});
		return user;
	}

	private ModifiableUserState getUserState(String id)
	{
		DefaultUserState state = null;
		if( id != null )
		{
			state = new DefaultUserState();
			UserBean bean = getInformationForUser(id);
			state.setLoggedInUser(bean);
		}
		return state;
	}

	private void handleIn(String sql, Collection<?> values, RowCallbackHandler handler)
	{
		List<Object> newVals = new ArrayList<Object>();
		for( Object object : values )
		{
			if( object != null )
			{
				newVals.add(object);
			}
		}
		if( newVals.size() == 0 )
		{
			return;
		}

		final int total = newVals.size();
		final int iterations = (int) Math.ceil((double) total / MAX_IN_PARAMETERS);
		for( int i = 0; i < iterations; i++ )
		{
			final int thisChunkStart = i * MAX_IN_PARAMETERS;
			final int thisChunkSize = (total - thisChunkStart > MAX_IN_PARAMETERS ? MAX_IN_PARAMETERS : total
				- thisChunkStart);
			final List<Object> chunkValues = newVals.subList(thisChunkStart, thisChunkStart + thisChunkSize);

			int inIndex = sql.toLowerCase().indexOf("in ?");
			final StringBuilder buff = new StringBuilder();

			int startIndex = 0;
			while( inIndex > 0 )
			{
				buff.append(sql.subSequence(startIndex, inIndex));
				buff.append("IN ");
				buff.append('(');
				final Iterator<?> it = chunkValues.iterator();
				for( int j = 0; it.hasNext(); j++ )
				{
					it.next();
					if( j != 0 )
					{
						buff.append(',');
					}
					buff.append('?');
				}
				buff.append(')');

				startIndex = inIndex + 4;
				inIndex = sql.toLowerCase().indexOf("in ?", inIndex + 1);
			}

			if( inIndex < 0 )
			{
				buff.append(sql.substring(startIndex));
			}
			executeSql(buff.toString(), chunkValues.toArray(), handler);
		}
	}

	private List<String> performSearch(String sql, String query)
	{
		return getIds(sql, query.replace('*', '%'));
	}

	private List<String> performSearch(String sql, String query, String param)
	{
		return getIds(sql, getParamValues(sql, query.replace('*', '%'), param));
	}

	private List<String> getIds(String sql, String param)
	{
		return getIds(sql, getParamValues(sql, param));
	}

	private List<String> getIds(String sql, Object[] values)
	{
		final List<String> ids = new ArrayList<String>();
		executeSql(sql, values, new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet set) throws SQLException
			{
				ids.add(set.getString(1));
			}
		});
		return ids;
	}

	private Map<String, GroupBean> getGroupInfo(Collection<String> ids)
	{
		final Map<String, GroupBean> groupMap = new HashMap<String, GroupBean>();
		String sql = config.getGroupInfo();
		if( Check.isEmpty(sql) || Check.isEmpty(ids) )
		{
			return groupMap;
		}

		handleIn(sql, ids, new RowCallbackHandler()
		{
			@Override
			public void processRow(ResultSet set) throws SQLException
			{
				String id = set.getString(1);
				String name = set.getString(2);
				GroupBean group = new DefaultGroupBean(id, name);
				groupMap.put(id, group);
			}
		});
		return groupMap;
	}

	@Override
	public Collection<GroupBean> searchGroups(final String search)
	{
		String sql = config.getSearchGroups();
		if( Check.isEmpty(sql) )
		{
			return null;
		}

		return getGroupInfo(performSearch(sql, search)).values();
	}

	@Override
	public Collection<GroupBean> searchGroups(String query, String parentId)
	{
		final String sql = config.getSearchGroupsInGroupRecursive();
		if( !Check.isEmpty(sql) )
		{
			return getGroupInfo(getIds(sql, getParamValues(sql, parentId, query.replace('*', '%')))).values();
		}

		final String nonRecurseSql = config.getSearchGroupsInGroup();
		if( !Check.isEmpty(nonRecurseSql) )
		{
			return getGroupInfo(performSearch(sql, query, parentId)).values();
		}

		return searchGroups(query);
	}

	private int getParamCount(String sql)
	{
		int count = 0;
		int i = sql.indexOf('?');
		for( ; i >= 0; count++ )
		{
			i = sql.indexOf('?', i + 1);
		}
		return count;
	}

	/**
	 * If params is shorter than the number of params in the SQL, the last param
	 * will replicated to fill in the missing spots (e.g. see the search query)
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	private Object[] getParamValues(String sql, Object... values)
	{
		final int sqlParamCount = getParamCount(sql);
		final Collection<Object> newValues = new ArrayList<Object>(sqlParamCount);
		for( int i = 0; i < sqlParamCount; i++ )
		{
			int sourceParamIndex = (i < values.length ? i : values.length - 1);
			// Sonar may recommend using Array asList method, but we're
			// hand-copying for a reason
			newValues.add(values[sourceParamIndex]); // NOSONAR
		}
		return newValues.toArray(new Object[newValues.size()]);
	}

	private void executeSql(String sql, Object[] params, RowCallbackHandler callback)
	{
		debugSql(sql, params);
		jdbcTemplate.query(sql, params, callback);
	}

	private void debugSql(String sql, Object[] params)
	{
		if( LOGGER.isDebugEnabled() )
		{
			String debugSql = sql;
			for( int i = 0; i < params.length; i++ )
			{
				debugSql = debugSql.replaceFirst("\\?", "'" + params[i].toString() + "'");
			}
			LOGGER.debug(debugSql);
		}
	}

	private static class UserInfo
	{
		private String id;
		private String password;

		public UserInfo()
		{
			super();
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(String password)
		{
			this.password = password;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}
	}
}
