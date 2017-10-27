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

package com.tle.core.services.user.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.tle.beans.Institution;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.user.UserService;
import com.tle.common.usermanagement.user.AbstractUserState;

/**
 * @author Nicholas Read
 */
public class RunAsUserState extends AbstractUserState
{
	private static final long serialVersionUID = 1L;
	private final TLEAclManager aclManager;
	private final UserService userService;
	private boolean doneAcls;
	private boolean doneGroups;
	private boolean doneRoles;
	private boolean system;

	public RunAsUserState(UserBean bean, Institution institution, TLEAclManager aclManager, UserService userService,
		boolean systemUser)
	{
		this.userService = userService;
		this.aclManager = aclManager;
		this.system = systemUser;
		setLoggedInUser(bean);
		setAuthenticated(true);
		setInstitution(institution);
		setSessionID(UUID.randomUUID().toString());
	}

	@Override
	public Set<String> getUsersRoles()
	{
		if( !doneRoles )
		{
			Set<String> roles = super.getUsersRoles();
			List<RoleBean> rolesForUser = userService.getRolesForUser(getUserBean().getUniqueID());
			for( RoleBean rolebean : rolesForUser )
			{
				roles.add(rolebean.getUniqueID());
			}
			doneRoles = true;
		}
		return super.getUsersRoles();
	}

	@Override
	public Set<String> getUsersGroups()
	{
		if( !doneGroups )
		{
			Set<String> usersGroups = super.getUsersGroups();
			usersGroups.addAll(userService.getGroupIdsContainingUser(getUserBean().getUniqueID()));
			doneGroups = true;
		}
		return super.getUsersGroups();
	}

	@Override
	public Collection<Long> getCommonAclExpressions()
	{
		ensureAcls();
		return super.getCommonAclExpressions();
	}

	@Override
	public Collection<Long> getNotOwnerAclExpressions()
	{
		ensureAcls();
		return super.getNotOwnerAclExpressions();
	}

	@Override
	public Collection<Long> getOwnerAclExpressions()
	{
		ensureAcls();
		return super.getOwnerAclExpressions();
	}

	@Override
	public boolean isSystem()
	{
		return system;
	}

	private void ensureAcls()
	{
		if( !doneAcls )
		{
			doneAcls = true;
			setAclExpressions(aclManager.getAclExpressions(this));
		}
	}
}
