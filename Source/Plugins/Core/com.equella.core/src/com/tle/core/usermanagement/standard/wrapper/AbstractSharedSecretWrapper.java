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

package com.tle.core.usermanagement.standard.wrapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.usermanagement.standard.wrapper.AbstractSharedSecretSettings;
import com.tle.beans.usermanagement.standard.wrapper.AbstractSharedSecretSettings.AbstractSharedSecretValue;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.usermanagement.util.TokenSecurity;
import com.tle.common.usermanagement.util.TokenSecurity.Token;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.security.impl.AclExpressionEvaluator;
import com.tle.core.usermanagement.standard.service.TLEUserService;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.exceptions.TokenException;
import com.tle.plugins.ump.AbstractUserDirectory;

/**
 * @author aholland
 */
public abstract class AbstractSharedSecretWrapper<S extends AbstractSharedSecretValue> extends AbstractUserDirectory
{
	private static final Logger LOGGER = Logger.getLogger(AbstractSharedSecretWrapper.class);

	@Inject
	private TLEUserService tleUserService;
	@Inject
	private RunAsInstitution runAs;

	protected Map<String, S> secrets;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		@SuppressWarnings("unchecked")
		List<S> secretsList = ((AbstractSharedSecretSettings<S>) settings).getSharedSecrets();

		secrets = new LinkedHashMap<String, S>(secretsList.size());
		for( S v : secretsList )
		{
			secrets.put(v.getId(), v);
		}

		return false;
	}

	@Override
	public ModifiableUserState authenticateToken(String token)
	{
		if( secrets.isEmpty() )
		{
			return null;
		}

		Token data = TokenSecurity.getInsecureToken(token);
		// if the secret id doesn't match one of ours, then we don't want it
		if( !isAcceptableToken(data) )
		{
			return null;
		}

		final S secret = secrets.get(data.getId());
		boolean autoCreate = false;
		String username = getUsername(secret, data);
		autoCreate = isAutoCreate(secret);

		ModifiableUserState state = getChain().authenticateUserFromUsername(username, data.getData());
		if( state == null )
		{
			if( autoCreate )
			{
				createUser(username, secret.getGroups());
				state = getChain().authenticateUserFromUsername(username, data.getData());
			}
			else if( !isIgnoreNonExistantUser(secret) )
			{
				throw new TokenException(TokenException.STATUS_USERNOTFOUND, username);
			}
		}

		if( state != null )
		{
			state.setToken(token);
			state.setTokenSecretId(data.getId());
			state.setAuditable(isAuditable());
		}

		return state;
	}

	public abstract boolean isAuditable();

	private void createUser(final String username, final List<String> groups)
	{
		runAs.executeAsSystem(CurrentInstitution.get(), new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					LOGGER.info("Creating new user from shared secret with username " + username);
					tleUserService.add(username, groups);
				}
				catch( Throwable t )
				{
					LOGGER.error("Error creating user " + username + " from shared secret ", t);
				}
			}
		});
	}

	@Override
	public VerifyTokenResult verifyUserStateForToken(UserState userState, String token)
	{
		try
		{
			Token data = TokenSecurity.getInsecureToken(token);

			if( !isAcceptableToken(data) )
			{
				return VerifyTokenResult.PASS;
			}

			S value = secrets.get(data.getId());

			AclExpressionEvaluator evaluator = new AclExpressionEvaluator();
			String expression = value.getExpression();
			if( Check.isEmpty(expression) )
			{
				LOGGER.info("No ACL expression supplied for id:'" + data.getId()); //$NON-NLS-1$
				return VerifyTokenResult.INVALID;
			}

			String tokenUser = getUsername(value, data);
			UserBean loggedInUser = userState.getUserBean();
			boolean eval = evaluator.evaluate(expression, userState, false);
			if( eval && (tokenUser.equalsIgnoreCase(loggedInUser.getUsername())
				|| tokenUser.equalsIgnoreCase(loggedInUser.getUniqueID())) )
			{
				// This is the only case where a token is valid
				return VerifyTokenResult.VALID;
			}

			if( !eval )
			{
				LOGGER.info("Expression '" + expression + "' did not evaluate to true"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				LOGGER.info("Token username '" + data.getInsecure() + "' does not match logged in username '" //$NON-NLS-1$ //$NON-NLS-2$
					+ userState.getUserBean().getUsername() + "'"); //$NON-NLS-1$
			}

			return VerifyTokenResult.INVALID;
		}
		catch( Exception e )
		{
			LOGGER.error("Error verifying", e); //$NON-NLS-1$
			return VerifyTokenResult.PASS;
		}
	}

	/**
	 * @param token
	 * @return
	 * @throws TokenException
	 */
	protected boolean isAcceptableToken(Token token)
	{
		boolean acceptable = false;
		try
		{
			if( token != null )
			{
				S secret = secrets.get(token.getId());
				if( secret == null )
				{
					throw new TokenException(TokenException.STATUS_BAD_ID, token.getId());
				}
				acceptable = TokenSecurity.isSecureToken(token, secret.getSecret());
			}
		}
		catch( TokenException e )
		{
			throw e; // NOSONAR
		}
		catch( Exception e )
		{
			throw new TokenException(TokenException.STATUS_UNKNOWN);
		}
		return acceptable;
	}

	@Override
	public List<String> getTokenSecretIds()
	{
		return new ArrayList<String>(secrets.keySet());
	}

	protected abstract boolean isIgnoreNonExistantUser(S value);

	protected abstract String getUsername(S value, Token token);

	protected abstract boolean isAutoCreate(S value);

}
