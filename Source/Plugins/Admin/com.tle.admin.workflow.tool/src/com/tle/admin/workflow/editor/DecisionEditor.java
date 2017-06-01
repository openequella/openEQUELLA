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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.dytech.edge.admin.script.ScriptEditor;
import com.dytech.edge.admin.script.workflowmodel.WorkflowModel;
import com.dytech.gui.TableLayout;
import com.tle.admin.Driver;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.DecisionNode;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.core.remoting.RemoteUserService;

public class DecisionEditor extends NodeEditor
{
	private static final long serialVersionUID = 1L;

	private final Driver driver;

	public DecisionEditor(final Driver driver)
	{
		super(driver.getClientService().getService(RemoteUserService.class), driver.getClientService().getService(
			RemoteSchemaService.class), "com.tle.admin.workflow.editor.decisioneditor.title"); //$NON-NLS-1$
		this.driver = driver;
	}

	@Override
	protected WorkflowNodePanel generatePanel()
	{
		return new WorkflowItemPanel();
	}

	public class WorkflowItemPanel extends WorkflowNodePanel implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		private JLabel scriptingLabel;
		private JButton scriptingButton;
		private String scriptUUID;
		private String script;

		public WorkflowItemPanel()
		{
			setup();
		}

		@Override
		public void load(final WorkflowNode node)
		{
			changeDetector.setIgnoreChanges(true);

			super.load(node);
			final DecisionNode item = (DecisionNode) node;
			script = item.getScript();
			scriptUUID = item.getCollectionUuid();

			// see Jira Defect TLE-604 :
			// http://apps.dytech.com.au/jira/browse/TLE-604
			updateScriptMessage();

			changeDetector.setIgnoreChanges(false);
		}

		@Override
		public void save(final WorkflowNode node)
		{
			final DecisionNode item = (DecisionNode) node;
			item.setScript(script);
			item.setCollectionUuid(scriptUUID);
			super.save(item);
		}

		public void launchScriptEditor()
		{
			final WorkflowModel model = new WorkflowModel(driver, scriptUUID);
			final ScriptEditor s = new ScriptEditor(model);
			s.importScript(script);
			s.showEditor(this);

			if( s.scriptWasSaved() )
			{
				script = s.getScript();
				scriptUUID = model.getUUID();
				changeDetector.forceChange(null);
			}
		}

		private void updateScriptMessage()
		{
			if( script == null || script.trim().length() == 0 )
			{
				scriptingLabel.setText(CurrentLocale.get("com.tle.admin.workflow.editor.decisioneditor.noscripting")); //$NON-NLS-1$
			}
			else
			{
				scriptingLabel.setText(CurrentLocale.get("com.tle.admin.workflow.editor.decisioneditor.notexecuted")); //$NON-NLS-1$
			}
		}

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if( e.getSource() == scriptingButton )
			{
				launchScriptEditor();
				updateScriptMessage();
			}
		}

		@Override
		protected void setup()
		{
			final JComponent south = createSouth();
			final JComponent north = createNamePanel(new JPanel());
			final JSeparator separator1 = new JSeparator();
			final JSeparator separator2 = new JSeparator();

			final int height3 = north.getPreferredSize().height;
			final int height2 = separator1.getPreferredSize().height;
			final int height4 = south.getPreferredSize().height;

			final int[] rows = {height3, 5, height2, 5, height4, 5, height2};
			final int[] cols = {600};

			setLayout(new TableLayout(rows, cols));

			add(north, new Rectangle(0, 0, 1, 1));
			add(separator1, new Rectangle(0, 2, 1, 1));
			add(south, new Rectangle(0, 4, 1, 1));
			add(separator2, new Rectangle(0, 6, 1, 1));
		}

		private JComponent createSouth()
		{
			scriptingLabel = new JLabel();
			scriptingButton = new JButton(CurrentLocale.get("com.tle.admin.workflow.editor.decisioneditor.open"));

			scriptingButton.addActionListener(this);

			final int width1 = scriptingButton.getPreferredSize().width;
			final int height1 = scriptingButton.getPreferredSize().height;

			final int[] rows = {height1,};
			final int[] cols = {width1, TableLayout.FILL,};

			final JPanel all = new JPanel(new TableLayout(rows, cols));
			all.add(scriptingLabel, new Rectangle(1, 0, 1, 1));
			all.add(scriptingButton, new Rectangle(0, 0, 1, 1));

			updateScriptMessage();

			return all;
		}

	}
}
