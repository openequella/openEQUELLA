/*
 * Created on Mar 16, 2005
 */
package com.tle.core.usermanagement.standard.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.beans.ump.RoleMapping;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.usermanagement.standard.wrapper.RoleWrapperSettings;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.usermanagement.user.valuebean.DefaultRoleBean;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.util.TLEPattern;
import com.tle.core.guice.Bind;
import com.tle.core.security.impl.AclExpressionEvaluator;
import com.tle.common.usermanagement.user.DefaultUserState;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.plugins.ump.AbstractUserDirectory;
import com.tle.plugins.ump.UserDirectoryUtils;

@Bind
public class TLERoleWrapper extends AbstractUserDirectory
{
	private Map<String, RoleMapping> mappings;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		mappings = new HashMap<String, RoleMapping>();
		for( RoleMapping mapping : ((RoleWrapperSettings) settings).getRoles() )
		{
			mappings.put(mapping.getId(), mapping);
		}

		return false;
	}

	@Override
	public Pair<ChainResult, Collection<RoleBean>> getRolesForUser(final String userID)
	{
		UserBean userBean = getChain().getInformationForUser(userID);
		if( userBean == null )
		{
			return null;
		}

		DefaultUserState state = new DefaultUserState();
		state.setLoggedInUser(userBean);

		Set<String> groups = state.getUsersGroups();
		for( GroupBean group : getChain().getGroupsContainingUser(userID) )
		{
			groups.add(group.getUniqueID());
		}

		return new Pair<ChainResult, Collection<RoleBean>>(ChainResult.CONTINUE, getInformationForRoles(
			getRolesForUser(state)).values());
	}

	@Override
	public Collection<RoleBean> searchRoles(final String query)
	{
		Collection<RoleBean> roles = new ArrayList<RoleBean>();
		for( RoleMapping mapping : mappings.values() )
		{
			if( TLEPattern.matches(query, mapping.getName()) )
			{
				roles.add(getInformationForRole(mapping.getId()));
			}
		}
		return roles;
	}

	private List<String> getRolesForUser(UserState state)
	{
		List<String> roles = new ArrayList<String>();

		AclExpressionEvaluator evaluator = new AclExpressionEvaluator();
		for( RoleMapping mapping : mappings.values() )
		{
			String expression = mapping.getExpression();
			if( !Check.isEmpty(expression) )
			{
				if( evaluator.evaluate(expression, state, false) )
				{
					roles.add(mapping.getId());
				}
			}
		}

		return roles;
	}

	@Override
	public RoleBean getInformationForRole(String roleId)
	{
		RoleMapping mapping = mappings.get(roleId);
		if( mapping == null )
		{
			return null;
		}
		return new DefaultRoleBean(mapping.getId(), mapping.getName());
	}

	@Override
	public Map<String, RoleBean> getInformationForRoles(Collection<String> roleIds)
	{
		return UserDirectoryUtils.getMultipleRoleInfosFromSingleInfos(this, roleIds);
	}

	@Override
	public void initUserState(ModifiableUserState state)
	{
		state.getUsersRoles().addAll(getRolesForUser(state));
	}

	@Override
	public void initGuestUserState(ModifiableUserState state)
	{
		state.getUsersRoles().addAll(getRolesForUser(state));
	}
}
