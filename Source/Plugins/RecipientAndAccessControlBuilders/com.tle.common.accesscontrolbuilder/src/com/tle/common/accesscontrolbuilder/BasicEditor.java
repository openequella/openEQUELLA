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

package com.tle.common.accesscontrolbuilder;

import static com.tle.common.security.SecurityConstants.LOGGED_IN_USER_ROLE_ID;
import static com.tle.common.security.SecurityConstants.getRecipient;
import static com.tle.common.security.SecurityConstants.Recipient.EVERYONE;
import static com.tle.common.security.SecurityConstants.Recipient.OWNER;
import static com.tle.common.security.SecurityConstants.Recipient.ROLE;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class BasicEditor implements PrivilegeListEditor
{
	private final RemoteUserService userService;

	public BasicEditor(RemoteUserService userService)
	{
		this.userService = userService;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.security.editors.PrivilegeListEditor#canHandle(com.tle.
	 * common.security.PrivilegeTree.Node,
	 * com.tle.admin.security.editors.PrivilegeList)
	 */
	@Override
	public boolean canHandle(Node privNode, PrivilegeList list)
	{
		return getModeForPrivilegeList(privNode, list) != null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.security.editors.PrivilegeListEditor#createView(java.lang
	 * .Object, com.tle.common.security.PrivilegeTree.Node,
	 * com.tle.admin.security.editors.PrivilegeList)
	 */
	@Override
	public JComponent createView(Object domainObj, Node privNode, PrivilegeList list)
	{
		return new BasicEditorPanel(this, userService, privNode, list);
	}

	public Mode getModeForPrivilegeList(Node privNode, PrivilegeList list)
	{
		List<PrivilegeListEntry> entries = list.getEntries();
		if( entries == null )
		{
			entries = Collections.emptyList();
		}

		// Make sure none of the privileges are overrides...
		for( PrivilegeListEntry entry : entries )
		{
			if( entry.isOverride() )
			{
				return null;
			}
		}

		if( entries.size() == 1 )
		{
			PrivilegeListEntry entry = entries.get(0);
			if( entry.isGranted() )
			{
				String who = entry.getWho();

				// Check if this should be the "everyone" option
				if( who.equals(getRecipient(EVERYONE)) )
				{
					return Mode.EVERYONE;
				}

				// Check if this should be the "everyone but guest" option
				if( who.equals(getRecipient(ROLE, LOGGED_IN_USER_ROLE_ID)) )
				{
					return Mode.EVERYONE_BUT_GUESTS;
				}

				// Check if this is just for the owner - no override/default
				// priorities allowed
				boolean overrideDefault = PrivilegeTree.isOverrideDefault(privNode, list.getPrivilege());
				if( who.equals(getRecipient(OWNER)) && !overrideDefault )
				{
					return Mode.JUST_THE_OWNER;
				}
			}
		}

		if( entries.size() >= 1 )
		{
			for( Iterator<PrivilegeListEntry> iter = entries.iterator(); iter.hasNext(); )
			{
				PrivilegeListEntry entry = iter.next();
				if( iter.hasNext() ^ entry.isGranted() )
				{
					return null;
				}

				if( !entries.get(entries.size() - 1).getWho().equals(getRecipient(EVERYONE)) )
				{
					return null;
				}
			}
			return Mode.LIMITED_SET;
		}

		return null;
	}

	public enum Mode
	{
		JUST_THE_OWNER, EVERYONE, EVERYONE_BUT_GUESTS, LIMITED_SET;
	}
}
