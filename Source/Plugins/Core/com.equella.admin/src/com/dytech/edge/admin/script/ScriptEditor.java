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

package com.dytech.edge.admin.script;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.JStatusBar;
import com.dytech.gui.TableLayout;
import com.tle.admin.common.gui.EditorHelper;
import com.tle.admin.gui.common.actions.CloseAction;
import com.tle.admin.gui.common.actions.OkAction;
import com.tle.client.gui.StatusBarContainer;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class ScriptEditor extends JPanel implements StatusBarContainer
{
	private static final long serialVersionUID = 1L;

	private boolean cancelled = true;

	private JDialog dialog;
	private JPanel content;

	private ScriptPanel scriptPanel;
	private JStatusBar statusbar;

	public ScriptEditor(ScriptModel model)
	{
		createGUI(model);
	}

	public void showEditor(Component parent)
	{
		dialog = ComponentHelper.createJDialog(parent);
		dialog.setContentPane(content);
		dialog.setTitle(CurrentLocale.get("com.dytech.edge.admin.script.scripteditor.title")); //$NON-NLS-1$
		dialog.setModal(true);

		ComponentHelper.percentageOfScreen(dialog, 0.85f, 0.7f);
		ComponentHelper.centreOnScreen(dialog);

		dialog.setVisible(true);
	}

	public void importScript(String script)
	{
		scriptPanel.importScript(script);
	}

	public boolean scriptWasSaved()
	{
		return !cancelled;
	}

	public String getScript()
	{
		return scriptPanel.getScript();
	}

	private void createGUI(ScriptModel model)
	{
		scriptPanel = new ScriptPanel(model, this);
		statusbar = new JStatusBar(EditorHelper.getStatusBarSpinner());

		JButton save = new JButton(new OkAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cancelled = false;
				dialog.dispose();
			}
		});

		JButton close = new JButton(new CloseAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if( scriptPanel.hasDetectedChanges() )
				{
					String message = CurrentLocale.get("com.dytech.edge.admin.script.scripteditor.message"); //$NON-NLS-1$
					String[] buttons = {CurrentLocale.get("com.dytech.edge.admin.gui.save"), //$NON-NLS-1$
							CurrentLocale.get("com.dytech.edge.admin.gui.dontsave"), //$NON-NLS-1$
							CurrentLocale.get("com.dytech.edge.admin.gui.cancel")}; //$NON-NLS-1$

					final int choice = JOptionPane.showOptionDialog(dialog, message,
						CurrentLocale.get("com.dytech.edge.admin.script.scripteditor.confirm"), //$NON-NLS-1$
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, buttons, buttons[2]);

					if( choice == JOptionPane.CANCEL_OPTION )
					{
						return;
					}
					else if( choice == JOptionPane.YES_OPTION )
					{
						cancelled = false;
					}
				}
				dialog.dispose();
			}
		});

		final int width = close.getPreferredSize().width;
		final int height = close.getPreferredSize().height;

		final int[] rows = {TableLayout.FILL, height};
		final int[] cols = {TableLayout.FILL, width, width};

		JPanel mainContainer = new JPanel();
		mainContainer.setLayout(new TableLayout(rows, cols, 5, 5));
		mainContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainContainer.add(scriptPanel, new Rectangle(0, 0, 3, 1));
		mainContainer.add(save, new Rectangle(1, 1, 1, 1));
		mainContainer.add(close, new Rectangle(2, 1, 1, 1));

		content = statusbar.attachToPanel(mainContainer);
	}

	@Override
	public JStatusBar getStatusBar()
	{
		return statusbar;
	}
}
