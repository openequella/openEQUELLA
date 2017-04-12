package com.tle.web.core.filter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.dytech.edge.exceptions.WebException;
import com.tle.common.hash.Hash;
import com.tle.core.guice.Bind;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.SchemaInfo;
import com.tle.core.system.SystemConfigService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.SystemUserState;
import com.tle.core.user.UserState;
import com.tle.web.core.filter.UserStateResult.Result;

/**
 * Stolen from EPS - allow OAuth token to set system user for Institution
 * manipulation
 * 
 * @author Dustin
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class SystemUserStateHook implements UserStateHook
{
	@Inject
	private SystemConfigService sysConfig;
	@Inject
	private MigrationService migrationService;

	@Override
	public UserStateResult getUserState(HttpServletRequest request, UserState userState) throws WebException
	{
		// X-Authorization header
		String xauth = request.getHeader("Authorization");
		if(xauth == null)
		{
			xauth = request.getHeader("X-Authorization");
		}

		if( xauth != null )
		{
			final String[] token = xauth.split("=");

			String tokenType = token[0];
			if( tokenType.equals("system_token") )
			{
				SchemaInfo status = migrationService.getSystemSchemaInfo();
				if( status.isInitial() && status.isMigrationRequired() )
				{
					migrationService.refreshSystemSchema();
					status = migrationService.getSystemSchemaInfo();
					if( status.isInitial() )
					{
						UserState state = new SystemUserState(null);
						state.setAuditable(false);
						return new UserStateResult(state, Result.LOGIN);
					}
				}

				sysConfig.checkAdminPassword(ensurePassword(token));
				UserState state = new SystemUserState(null);
				state.setAuditable(false);
				return new UserStateResult(state, Result.LOGIN);

			}
			else if( tokenType.equals("admin_token") )
			{
				if( Hash.checkPasswordMatch(CurrentInstitution.get().getAdminPassword(), ensurePassword(token)) )
				{
					SystemUserState state = new SystemUserState(CurrentInstitution.get());
					state.setAuditable(false);
					return new UserStateResult(state, Result.LOGIN);
				}
				else
				{
					throw new WebException(403, "access_denied", "Incorrect password");
				}
			}
		}
		return null;
	}

	private String ensurePassword(String[] token)
	{
		if( token.length != 2 )
		{
			throw new WebException(403, "access_denied", "Invalid access_token format in X-Authorization header");
		}
		return token[1];
	}

	@Override
	public boolean isInstitutional()
	{
		return false;
	}
}
