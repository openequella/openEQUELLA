/*
 * Created on Mar 16, 2005
 */
package com.tle.core.usermanagement.standard.wrapper;

import static com.tle.common.security.SecurityConstants.GUEST_USER_ROLE_ID;
import static com.tle.common.security.SecurityConstants.LOGGED_IN_USER_ROLE_ID;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.common.hash.Hash;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.settings.standard.MailSettings;
import com.tle.common.usermanagement.user.valuebean.DefaultRoleBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.util.TLEPattern;
import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.plugins.ump.UserDirectoryUtils;

@Bind
@SuppressWarnings("nls")
public class SpecialAdminWrapper extends AbstractSystemUserWrapper
{
	private final Map<String, RoleBean> roles;

	private String email;

	public SpecialAdminWrapper()
	{
		roles = Maps.newHashMap();
		roles.put(LOGGED_IN_USER_ROLE_ID, new DefaultRoleBean(LOGGED_IN_USER_ROLE_ID, "Logged In User Role"));
		roles.put(GUEST_USER_ROLE_ID, new DefaultRoleBean(GUEST_USER_ROLE_ID, "Guest User Role"));
	}

	@Inject
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.email = configurationService.getProperties(new MailSettings()).getSender();
	}

	@Override
	protected boolean authenticatePassword(String suppliedPassword)
	{
		return Hash.checkPasswordMatch(CurrentInstitution.get().getAdminPassword(), suppliedPassword);
	}

	@Override
	protected String getEmailAddress()
	{
		return email;
	}

	@Override
	public void initUserState(ModifiableUserState state)
	{
		state.getUsersRoles().add(LOGGED_IN_USER_ROLE_ID);
	}

	@Override
	public RoleBean getInformationForRole(String roleID)
	{
		return roles.get(roleID);
	}

	@Override
	public Map<String, RoleBean> getInformationForRoles(Collection<String> roleIds)
	{
		return UserDirectoryUtils.getMultipleRoleInfosFromSingleInfos(this, roleIds);
	}

	@Override
	public List<RoleBean> searchRoles(String query)
	{
		List<RoleBean> rv = null;
		for( RoleBean roleBean : roles.values() )
		{
			if( TLEPattern.matches(query, roleBean.getName()) )
			{
				if( rv == null )
				{
					rv = Lists.newArrayListWithExpectedSize(roles.size());
				}
				rv.add(roleBean);
			}
		}
		return rv;
	}
}
