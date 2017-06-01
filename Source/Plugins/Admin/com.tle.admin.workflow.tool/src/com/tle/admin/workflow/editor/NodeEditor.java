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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.tle.admin.common.gui.EditorHelper;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

public class NodeEditor extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;

	protected final RemoteUserService userService;
	protected final RemoteSchemaService schemaService;
	private final String dialogTitleKey;

	protected WorkflowNodePanel pane;
	protected ChangeDetector changeDetector;
	protected JButton save;

	protected JButton close;

	private boolean savePressed;
	private JDialog dialog;

	public NodeEditor(final RemoteUserService userService, final RemoteSchemaService schemaService,
		final String dialogTitleKey)
	{
		this.userService = userService;
		this.schemaService = schemaService;
		this.dialogTitleKey = dialogTitleKey;
		setup();
	}

	public boolean showEditor(final Component parent)
	{
		savePressed = false;

		dialog = ComponentHelper.createJDialog(parent);
		dialog.setModal(true);
		dialog.setTitle(CurrentLocale.get(dialogTitleKey));
		dialog.setContentPane(this);

		setupSize(dialog);
		ComponentHelper.centreOnScreen(dialog);

		dialog.setVisible(true);

		return savePressed;
	}

	protected void setupSize(final JDialog dialog)
	{
		dialog.pack();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e)
	{
		if( e.getSource() == save )
		{
			savePressed = true;
			closeDialog();
		}
		else if( e.getSource() == close )
		{
			if( !changeDetector.hasDetectedChanges() || !save.isEnabled() )
			{
				savePressed = false;
				closeDialog();
			}
			else
			{
				final String message = CurrentLocale.get("com.tle.admin.workflow.editor.nodeeditor.save");
				final String[] buttons = {CurrentLocale.get("com.dytech.edge.admin.gui.save"),
						CurrentLocale.get("com.dytech.edge.admin.gui.dontsave"),
						CurrentLocale.get("com.dytech.edge.admin.gui.cancel")};

				final int confirm = JOptionPane.showOptionDialog(this, message,
					CurrentLocale.get("com.tle.admin.workflow.editor.nodeeditor.savebefore"),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, buttons, buttons[2]);

				if( confirm == JOptionPane.YES_OPTION )
				{
					savePressed = true;
					closeDialog();
				}
				if( confirm == JOptionPane.NO_OPTION )
				{
					savePressed = false;
					closeDialog();
				}
			}
		}
	}

	private void closeDialog()
	{
		dialog.dispose();
		dialog = null;
	}

	private void setup()
	{
		changeDetector = new ChangeDetector();
		pane = generatePanel();

		save = new JButton(EditorHelper.DEFAULT_SAVE_TEXT);
		close = new JButton(EditorHelper.DEFAULT_CLOSE_TEXT);

		save.addActionListener(this);
		close.addActionListener(this);

		save.setEnabled(false);

		final int width1 = close.getPreferredSize().width;
		final int height1 = close.getPreferredSize().height;

		final Dimension dim1 = pane.getLayoutSizes();
		if( dim1.width != TableLayout.FILL )
		{
			dim1.width -= width1 * 2;
		}

		final int[] rows = {dim1.height, height1,};
		final int[] cols = {dim1.width, width1, width1,};

		setLayout(new TableLayout(rows, cols));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(pane, new Rectangle(0, 0, 3, 1));
		add(save, new Rectangle(1, 1, 1, 1));
		add(close, new Rectangle(2, 1, 1, 1));
	}

	protected WorkflowNodePanel generatePanel()
	{
		return new WorkflowNodePanel();
	}

	public class WorkflowNodePanel extends JPanel
	{
		private static final long serialVersionUID = 1L;

		protected I18nTextField nameField;
		protected JCheckBox reject;

		public WorkflowNodePanel()
		{
			setup();
		}

		public Dimension getLayoutSizes()
		{
			return getPreferredSize();
		}

		public void load(final WorkflowNode node1)
		{
			changeDetector.setIgnoreChanges(true);

			nameField.load(node1.getName());

			if( !node1.isLeafNode() )
			{
				reject.setSelected(((WorkflowTreeNode) node1).isRejectPoint());
			}

			changeDetector.setIgnoreChanges(false);
			save.setEnabled(true);
		}

		public void save(final WorkflowNode node)
		{
			LanguageBundle nameBundle = nameField.save();

			// Db restrictions mean that node names need to be >100 char
			Map<String, LanguageString> strings = nameBundle.getStrings();
			for( Entry<String, LanguageString> entry : strings.entrySet() )
			{
				LanguageString currentString = entry.getValue();
				if( currentString.getText().length() > 100 )
				{
					currentString.setText(currentString.getText().substring(0, 100));
					strings.put(entry.getKey(), currentString);
				}
			}
			node.setName(nameBundle);
			if( !node.isLeafNode() )
			{
				((WorkflowTreeNode) node).setRejectPoint(reject.isSelected());
			}
		}

		protected void setup()
		{
			createNamePanel(this);
		}

		protected JPanel createNamePanel(final JPanel panel)
		{
			final JLabel nameLabel = new JLabel(CurrentLocale.get("com.tle.admin.workflow.editor.nodeeditor.name"));

			nameField = new I18nTextField(BundleCache.getLanguages());

			reject = new JCheckBox(CurrentLocale.get("com.tle.admin.workflow.editor.nodeeditor.reject"));

			final int height1 = nameLabel.getPreferredSize().height;
			final int height2 = nameField.getPreferredSize().height;
			final int height3 = reject.getPreferredSize().height;

			final int[] rows = {height1, height2, height3};
			final int[] cols = {300};

			panel.setLayout(new TableLayout(rows, cols));

			panel.add(nameLabel, new Rectangle(0, 0, 1, 1));
			panel.add(nameField, new Rectangle(0, 1, 1, 1));
			panel.add(reject, new Rectangle(0, 2, 1, 1));

			changeDetector.watch(nameField);
			changeDetector.watch(reject);

			return panel;
		}
	}

	public void load(final WorkflowNode node)
	{
		pane.load(node);
	}

	public void save(final WorkflowNode node)
	{
		pane.save(node);
	}
}
