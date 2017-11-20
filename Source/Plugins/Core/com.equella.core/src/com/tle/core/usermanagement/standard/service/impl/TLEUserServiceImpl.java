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

package com.tle.core.usermanagement.standard.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.ValidationError;
import com.tle.core.services.ValidationHelper;
import com.tle.common.beans.exception.InvalidDataException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.beans.ump.RoleMapping;
import com.tle.beans.user.TLEUser;
import com.tle.beans.usermanagement.standard.wrapper.RoleWrapperSettings;
import com.tle.beans.usermanagement.standard.wrapper.SuspendedUserWrapperSettings;
import com.tle.common.Check;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.hash.Hash;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.email.EmailService;
import com.tle.core.events.GroupDeletedEvent;
import com.tle.core.events.GroupEditEvent;
import com.tle.core.events.GroupIdChangedEvent;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.GroupChangedListener;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.guice.Bind;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.core.events.services.EventService;
import com.tle.core.services.user.UserService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.usermanagement.standard.dao.TLEUserDao;
import com.tle.core.usermanagement.standard.service.TLEGroupService;
import com.tle.core.usermanagement.standard.service.TLEUserService;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;

/**
 * @author Nicholas Read
 */
@Singleton
@SuppressWarnings("nls")
@Bind(TLEUserService.class)
public class TLEUserServiceImpl implements TLEUserService, UserChangeListener, GroupChangedListener
{
	private static final Logger LOGGER = Logger.getLogger(TLEUserServiceImpl.class);
	private static final String[] BLANKS = {"username", "lastName", "firstName",};
	private static final int PASSWORD_MIN_LENGTH = 6;

	@Inject
	private TLEUserDao dao;
	@Inject
	private EventService eventService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private EmailService emailService;
	@Inject
	private UserService userService;
	@Inject
	private TLEGroupService groupService;

	@Override
	public String add(TLEUser newUser)
	{
		return add(newUser, true);
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional
	public String add(TLEUser newUser, boolean passwordNotHashed)
	{
		Check.checkNotNull(newUser);

		newUser.setInstitution(CurrentInstitution.get());
		newUser.setId(0l);

		if( Check.isEmpty(newUser.getUuid()) )
		{
			newUser.setUuid(UUID.randomUUID().toString());
		}
		else if( get(newUser.getUuid()) != null )
		{
			throw new RuntimeApplicationException("User with UUID already exists: " + newUser.getUuid());
		}

		validate(newUser, passwordNotHashed);
		if( passwordNotHashed )
		{
			hashPassword(newUser);
		}

		dao.save(newUser);
		// bug #7721
		userService.clearUserSearchCache();
		return newUser.getUuid();
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional
	public String add(TLEUser newUser, List<String> groups)
	{
		final String uuid = add(newUser);
		// WTF Oracle and Postgres?? EQ-1418
		dao.flush();

		for( String g : groups )
		{
			groupService.addUserToGroup(g, uuid);
		}

		return uuid;
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional
	public String add(String username, List<String> groups)
	{
		final TLEUser user = new TLEUser();
		user.setUsername(username);
		user.setFirstName(username);
		user.setLastName(username);
		user.setPassword(UUID.randomUUID().toString());

		return add(user, groups);
	}

	@Override
	@Transactional
	public List<TLEUser> searchUsers(String query, String parentGroupID, boolean recursive)
	{
		return dao.searchUsersInGroup(query, parentGroupID, recursive);
	}

	@Override
	@Transactional
	public TLEUser get(String id)
	{
		return dao
			.findByCriteria(Restrictions.eq("uuid", id), Restrictions.eq("institution", CurrentInstitution.get()));
	}

	@Override
	@Transactional
	public List<TLEUser> getInformationForUsers(Collection<String> ids)
	{
		return dao.getInformationForUsers(ids);
	}

	@Override
	@Transactional
	public TLEUser getByUsername(String username)
	{
		LOGGER.trace("Searching for user with username " + username);
		TLEUser result = dao.findByUsername(username);
		if( result == null )
		{
			LOGGER.trace("No user found with username " + username);
			return null;
		}
		dao.unlinkFromSession(result);
		LOGGER.trace("Found a user with username " + username);
		return result;
	}

	@Override
	@Transactional
	public String editSelf(TLEUser user, boolean passwordNotHashed)
	{
		if( CurrentUser.getUserID().equals(user.getUniqueID()) )
		{
			return editPrivate(user, passwordNotHashed);
		}
		throw new AccessDeniedException("You don't not have permissions to edit this user.");
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional
	public String edit(String uuid, String username, String password, String first, String last, String email)
	{
		boolean doAdd = false;

		TLEUser user = get(uuid);
		if( user == null )
		{
			user = new TLEUser();
			doAdd = true;
		}

		if( username != null )
		{
			user.setUsername(username);
		}

		if( email != null )
		{
			user.setEmailAddress(email);
		}

		if( first != null )
		{
			user.setFirstName(first);
		}

		if( last != null )
		{
			user.setLastName(last);
		}

		boolean passwordNotHashed = false;
		if( doAdd || password != null )
		{
			user.setPassword(password);
			passwordNotHashed = true;
		}

		return doAdd ? add(user) : edit(user, passwordNotHashed);
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional
	public String edit(TLEUser user, boolean passwordNotHashed)
	{
		return editPrivate(user, passwordNotHashed);
	}

	private String editPrivate(TLEUser user, boolean passwordNotHashed)
	{
		Check.checkNotNull(user);

		user.setInstitution(CurrentInstitution.get());

		validate(user, passwordNotHashed);

		if( passwordNotHashed )
		{
			hashPassword(user);
		}

		dao.update(user);
		dao.flush();

		String uuid = user.getUuid();
		eventService.publishApplicationEvent(new UserEditEvent(uuid));
		userService.clearUserSearchCache();
		return uuid;
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional
	public void delete(String uuid)
	{
		Check.checkNotEmpty(uuid);

		TLEUser user = get(uuid);
		if( user != null )
		{
			dao.delete(user);
			dao.flush();
			dao.clear();

			// Tell the world that we have deleted someone.
			eventService.publishApplicationEvent(new UserDeletedEvent(uuid));
			userService.clearUserSearchCache();
		}
		else
		{
			throw new NotFoundException("Cannot find user with ID " + uuid + " to delete.");
		}
	}

	@Override
	public void validate(TLEUser user, boolean passwordNotHashed)
	{
		List<ValidationError> errors = new ArrayList<ValidationError>();

		ValidationHelper.checkBlankFields(user, BLANKS, errors);

		// Make sure we have a valid email address
		if( !Check.isEmpty(user.getEmailAddress()) && !emailService.isValidAddress(user.getEmailAddress()) )
		{
			errors.add(new ValidationError("email", CurrentLocale.get("com.tle.web.userdetails.common.invalidemail")));
		}

		validatePassword(user.getPassword(), passwordNotHashed, errors);

		if( dao.doesOtherUsernameSameSpellingExist(user.getUsername(), user.getUuid()) )
		{
			errors.add(new ValidationError("username", "Username already exists: " + user.getUsername()));
		}

		if( !errors.isEmpty() )
		{
			throw new InvalidDataException(errors);
		}
	}

	@Override
	public void validatePassword(String password, boolean passwordNotHashed)
	{
		List<ValidationError> errors = new ArrayList<ValidationError>();
		validatePassword(password, passwordNotHashed, errors);
		if( !errors.isEmpty() )
		{
			throw new InvalidDataException(errors);
		}
	}

	private void validatePassword(String password, boolean passwordNotHashed, List<ValidationError> errors)
	{
		// Check password
		if( passwordNotHashed )
		{
			int len = password == null ? 0 : password.length();
			if( len < PASSWORD_MIN_LENGTH )
			{
				errors.add(new ValidationError("password", CurrentLocale
					.get("com.tle.web.userdetails.common.passwordtooshort")));
			}
		}
	}

	@Override
	public boolean checkPasswordMatch(TLEUser user, String password)
	{
		return Hash.checkPasswordMatch(user.getPassword(), password);
	}

	private void hashPassword(TLEUser user)
	{
		user.setPassword(Hash.hashPassword(user.getPassword()));
	}

	@Override
	public void userDeletedEvent(UserDeletedEvent event)
	{
		// Don't delete our users! Either we kicked it off by deleting one of
		// our users, or it's an event from another UMP which isn't going to
		// match our IDs anyway.

		// Do nothing about roles since their expressions - we're not sure what
		// to do.

		// Suspended users
		SuspendedUserWrapperSettings suws = configService.getProperties(new SuspendedUserWrapperSettings());
		if( suws.getSuspendedUsers().remove(event.getUserID()) )
		{
			configService.setProperties(suws);
		}
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// We don't care
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		// Unnecessary to update old user. The new user should already be
		// created and the old user deleted/redundant but we also look after
		// the roles and suspended users because they don't have services of
		// their own.

		// Roles
		updateRoles(SecurityConstants.getRecipient(Recipient.USER, event.getFromUserId()),
			SecurityConstants.getRecipient(Recipient.USER, event.getToUserId()));

		// Suspended users
		SuspendedUserWrapperSettings suws = configService.getProperties(new SuspendedUserWrapperSettings());
		if( suws.getSuspendedUsers().remove(event.getFromUserId()) )
		{
			suws.getSuspendedUsers().add(event.getToUserId());
			configService.setProperties(suws);
		}
	}

	@Override
	public void groupDeletedEvent(GroupDeletedEvent event)
	{
		// We look after the roles because they don't have services of their
		// own, but we can't go changing ACL expression to remove groups.
	}

	@Override
	public void groupEditedEvent(GroupEditEvent event)
	{
		// Don't care
	}

	@Override
	public void groupIdChangedEvent(GroupIdChangedEvent event)
	{
		updateRoles(SecurityConstants.getRecipient(Recipient.GROUP, event.getFromGroupId()),
			SecurityConstants.getRecipient(Recipient.GROUP, event.getToGroupId()));
	}

	private void updateRoles(String findThis, String replaceWithThis)
	{
		final RoleWrapperSettings roles = configService.getProperties(new RoleWrapperSettings());

		boolean rolesChanged = false;
		for( RoleMapping rm : roles.getRoles() )
		{
			String expression = rm.getExpression();
			if( expression.contains(findThis) )
			{
				rm.setExpression(expression.replace(findThis, replaceWithThis));
				rolesChanged = true;
			}
		}

		if( rolesChanged )
		{
			configService.setProperties(roles);
		}
	}

	@Override
	public String prepareQuery(String searchString)
	{
		if( !searchString.startsWith("*") )
		{
			searchString = "*" + searchString;
		}

		if( !searchString.endsWith("*") )
		{
			searchString += "*";
		}

		return searchString;
	}

}
