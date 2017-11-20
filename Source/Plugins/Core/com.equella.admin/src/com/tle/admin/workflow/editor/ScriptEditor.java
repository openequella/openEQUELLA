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

package com.tle.admin.workflow.editor;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import com.dytech.gui.TableLayout;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.core.remoting.RemoteUserService;

public class ScriptEditor extends NodeEditor
{
	private static final long serialVersionUID = 1L;

	public ScriptEditor(final RemoteUserService userService, final RemoteSchemaService schemaService)
	{
		super(userService, schemaService, "com.tle.admin.workflow.editor.scripteditor.title");
	}

	@Override
	protected void setupSize(final JDialog dialog)
	{
		dialog.setSize(885, 925);
	}

	@Override
	protected WorkflowNodePanel generatePanel()
	{
		return new WorkflowItemPanel();
	}

	public class WorkflowItemPanel extends WorkflowNodePanel
	{
		private static final long serialVersionUID = 1L;
		private ScriptTab scriptTab;
		private NotificationsTab notificationsTab;

		public WorkflowItemPanel()
		{
			super();
		}

		@Override
		public Dimension getLayoutSizes()
		{
			return new Dimension(TableLayout.FILL, TableLayout.FILL);
		}

		@Override
		public void load(final WorkflowNode node)
		{
			changeDetector.setIgnoreChanges(true);
			final ScriptNode scriptNode = (ScriptNode) node;
			scriptTab.load(scriptNode);
			notificationsTab.load(scriptNode);
			save.setEnabled(true);
			changeDetector.setIgnoreChanges(false);
		}

		@Override
		public void save(WorkflowNode node)
		{
			final ScriptNode scriptNode = (ScriptNode) node;
			scriptTab.save(scriptNode);
			notificationsTab.save(scriptNode);
		}

		@Override
		protected void setup()
		{
			scriptTab = new ScriptTab(changeDetector);
			notificationsTab = new NotificationsTab(changeDetector, userService);
			final JTabbedPane tabs = new JTabbedPane();
			tabs.add(CurrentLocale.get("com.tle.admin.workflow.editor.scripteditor.details"), scriptTab);
			tabs.add(CurrentLocale.get("com.tle.admin.workflow.editor.stepeditor.notifications"), notificationsTab);

			setLayout(new GridLayout(1, 1));
			add(tabs);
		}
	}
}
