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

package com.tle.admin.usermanagement.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.gui.filter.FilterModel;
import com.dytech.gui.filter.FilteredShuffleBox;
import com.tle.admin.Driver;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.beans.usermanagement.standard.wrapper.SuspendedUserWrapperSettings;
import com.tle.common.Format;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.remoting.RemoteUserService;

public class SuspensionWrapper extends GeneralPlugin<SuspendedUserWrapperSettings>
{
	private static final Log LOGGER = LogFactory.getLog(SuspensionWrapper.class);
	private FilteredShuffleBox<UserBean> fsb;

	public SuspensionWrapper()
	{
		fsb = new FilteredShuffleBox<UserBean>(new GroupFilter());
		addFillComponent(fsb);
	}

	@Override
	public void load(SuspendedUserWrapperSettings xml)
	{
		RemoteUserService userService = clientService.getService(RemoteUserService.class);
		try
		{
			List<UserBean> users = new ArrayList<UserBean>(userService.getInformationForUsers(xml.getSuspendedUsers())
				.values());
			Collections.sort(users, Format.USER_BEAN_COMPARATOR);
			fsb.addToRight(users);
		}
		catch( RuntimeApplicationException e )
		{
			displayError(CurrentLocale.get("com.tle.admin.usermanagement.suspensionwrapper.errorloading"), e); //$NON-NLS-1$
		}
	}

	@Override
	public boolean save(SuspendedUserWrapperSettings xml)
	{
		Set<String> right = new HashSet<String>();
		for( UserBean user : fsb.getRight() )
		{
			right.add(user.getUniqueID());
		}

		boolean saved = false;
		try
		{
			xml.setSuspendedUsers(right);
			saved = true;
		}
		catch( Exception e )
		{
			displayError(CurrentLocale.get("com.tle.admin.usermanagement.suspensionwrapper.errorsaving"), e); //$NON-NLS-1$
		}

		return saved;
	}

	protected class GroupFilter extends FilterModel<UserBean>
	{
		@Override
		public List<UserBean> search(String query)
		{
			try
			{
				List<UserBean> users = clientService.getService(RemoteUserService.class).searchUsers(query);
				Collections.sort(users, Format.USER_BEAN_COMPARATOR);
				return users;
			}
			catch( RuntimeApplicationException e )
			{
				displayError(CurrentLocale.get("com.tle.admin.usermanagement.suspensionwrapper.errorsearching"), e); //$NON-NLS-1$
				return Collections.emptyList();
			}
		}
	}

	protected void displayError(String s, Exception e)
	{
		LOGGER.error(s, e);
		Driver.displayInformation(fsb, s + " : " + e.getMessage()); //$NON-NLS-1$
	}
}
