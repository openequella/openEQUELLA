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

package com.tle.admin.usermanagement.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.beans.user.TLEGroup;
import com.tle.beans.user.TLEUser;
import com.tle.common.BulkImport;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.CsvReader;
import com.tle.core.remoting.RemoteTLEGroupService;
import com.tle.core.remoting.RemoteTLEUserService;

public class UserBulkImporter extends BulkImport<TLEUser>
{
	private final RemoteTLEGroupService groupService;
	private final RemoteTLEUserService userService;

	private boolean passwordNotHashed;
	private String groupName;

	public UserBulkImporter(RemoteTLEUserService userService, RemoteTLEGroupService groupService)
	{
		this.userService = userService;
		this.groupService = groupService;
	}

	@Override
	public void add(TLEUser t) throws Exception
	{
		TLEGroup group = null;
		if( !Check.isEmpty(groupName) )
		{
			group = groupService.getByName(groupName);
			if( group == null )
			{
				List<ValidationError> errors = new ArrayList<ValidationError>();
				errors.add(new ValidationError("group", //$NON-NLS-1$
					CurrentLocale.get("tleuserservice.bulkimport.nogroup", groupName) //$NON-NLS-1$
					));
				throw new InvalidDataException(errors);
			}
		}

		// add user, preferably avoiding inadvertent recursion
		String uuid = userService.add(t);

		// add to group
		if( group != null )
		{
			group.getUsers().add(uuid);
			groupService.edit(group);
		}
	}

	@Override
	public TLEUser createNew()
	{
		return new TLEUser();
	}

	@Override
	public void edit(TLEUser t)
	{
		userService.edit(t, passwordNotHashed);
	}

	@Override
	public TLEUser getOld(CsvReader reader) throws IOException
	{
		String uuid = reader.get("uuid"); //$NON-NLS-1$
		String username = reader.get("username"); //$NON-NLS-1$
		if( !Check.isEmpty(uuid) )
		{
			return userService.get(uuid);
		}
		else if( !Check.isEmpty(username) )
		{
			return userService.getByUsername(username);
		}
		return null;
	}

	@SuppressWarnings("nls")
	@Override
	public void update(CsvReader reader, TLEUser t, boolean create) throws IOException
	{
		if( create )
		{
			t.setUuid(reader.get("uuid"));
		}

		t.setEmailAddress(reader.get("email"));
		t.setFirstName(reader.get("firstname"));
		t.setLastName(reader.get("lastname"));
		t.setUsername(reader.get("username"));
		String password = reader.get("password");
		if( !Check.isEmpty(password) )
		{
			t.setPassword(password);
			passwordNotHashed = true;
		}
		else if( create )
		{
			List<ValidationError> errors = new ArrayList<ValidationError>();
			errors.add(new ValidationError("password", CurrentLocale.get("tleuserservice.bulkimport.nopassword")
				));
			throw new InvalidDataException(errors);
		}

		groupName = reader.get("group"); //$NON-NLS-1$
	}
}