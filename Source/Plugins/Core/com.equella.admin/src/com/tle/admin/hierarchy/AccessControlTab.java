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

package com.tle.admin.hierarchy;

import java.awt.GridLayout;

import com.dytech.gui.ChangeDetector;
import com.tle.admin.gui.EditorException;
import com.tle.admin.hierarchy.TopicEditor.AbstractTopicEditorTab;
import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.common.accesscontrolbuilder.AccessEditor;
import com.tle.common.applet.client.ClientService;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class AccessControlTab extends AbstractTopicEditorTab
{
	private static final long serialVersionUID = 1L;

	private final ClientService clientService;

	private AccessEditor editor;

	public AccessControlTab(ClientService clientService)
	{
		this.clientService = clientService;
	}

	@Override
	public void setup(ChangeDetector changeDetector)
	{
		editor = new AccessEditor(clientService.getService(RemoteTLEAclManager.class),
			clientService.getService(RemoteUserService.class));

		setLayout(new GridLayout(1, 1));
		add(editor);

		changeDetector.watch(editor);
	}

	@Override
	public void load(HierarchyPack pack)
	{
		TargetList targetList = pack.getTargetList();
		editor.load(pack.getTopic(), targetList, Node.HIERARCHY_TOPIC);
	}

	@Override
	public void save(HierarchyPack pack)
	{
		pack.setTargetList(editor.save());
	}

	@Override
	public void validation() throws EditorException
	{
		// nothing to validate
	}
}
