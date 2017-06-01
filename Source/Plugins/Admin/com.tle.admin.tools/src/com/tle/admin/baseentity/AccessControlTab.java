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

package com.tle.admin.baseentity;

import java.awt.Component;
import java.awt.GridLayout;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.accesscontrolbuilder.AccessEditor;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class AccessControlTab<T extends BaseEntity> extends BaseEntityTab<T>
{
	private final Node privilegeNode;

	private AccessEditor editor;

	public AccessControlTab(Node privilegeNode)
	{
		this.privilegeNode = privilegeNode;
	}

	@Override
	public void init(Component parent)
	{
		editor = new AccessEditor(clientService.getService(RemoteTLEAclManager.class),
			clientService.getService(RemoteUserService.class));

		setLayout(new GridLayout(1, 1));
		add(editor);
	}

	@Override
	public void validation()
	{
		// Nothing to validate here.
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.baseentity.accesscontroltab.title"); //$NON-NLS-1$
	}

	@Override
	public void load()
	{
		editor.load(state.getEntity(), state.getEntityPack().getTargetList(), privilegeNode);
	}

	@Override
	public void save()
	{
		state.getEntityPack().setTargetList(editor.save());
	}
}
