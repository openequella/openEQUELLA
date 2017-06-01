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

import java.util.List;

import javax.swing.JComponent;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class InheritedEditor implements PrivilegeListEditor
{
	private final RemoteTLEAclManager aclManager;
	private final RemoteUserService userService;

	public InheritedEditor(RemoteTLEAclManager aclManager, RemoteUserService userService)
	{
		this.aclManager = aclManager;
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
		List<PrivilegeListEntry> entries = list.getEntries();
		return entries == null || entries.isEmpty();
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
		List<PrivilegeListEntry> entries = list.getEntries();
		if( entries != null )
		{
			entries.clear();
		}
		return new InheritedEditorPanel(aclManager, userService, domainObj, list.getPrivilege());
	}
}
