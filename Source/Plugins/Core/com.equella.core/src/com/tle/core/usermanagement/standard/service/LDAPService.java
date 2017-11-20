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

package com.tle.core.usermanagement.standard.service;

import java.util.Collection;

import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.remoting.RemoteLDAPService;
import com.tle.core.usermanagement.standard.ldap.LDAP;

public interface LDAPService extends RemoteLDAPService
{
	String searchAuthenticate(LDAP ldap, String username, String password);

	String getTokenFromUsername(LDAP ldap, String username);

	UserBean getUserBean(LDAP ldap, String userID);

	Collection<GroupBean> getGroupsContainingUser(LDAP ldap, String userID);

	Collection<UserBean> getUsersInGroup(LDAP ldap, String query, String parentGroupID, boolean recursive);

	Collection<UserBean> searchUsers(LDAP ldap, String query);

	GroupBean getParentGroupForGroup(LDAP ldap, String groupID);

	GroupBean getGroupBean(LDAP ldap, String groupID);

	Collection<GroupBean> searchGroups(LDAP ldap, String query);

	Collection<GroupBean> searchGroups(LDAP ldap, String query, String parentGroupId);

	UserBean resolveUserFromToken(LDAP ldap, String token);
}
