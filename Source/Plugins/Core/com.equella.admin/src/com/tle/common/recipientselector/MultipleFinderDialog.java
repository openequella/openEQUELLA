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

package com.tle.common.recipientselector;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class MultipleFinderDialog implements ActionListener
{
	private JButton ok;
	private JButton cancel;

	private MultipleFinderControl control;

	private JPanel all;
	private JDialog dialog;
	private boolean returnResults;

	public MultipleFinderDialog(RemoteUserService userService)
	{
		setupGUI(userService);
	}

	private void setupGUI(RemoteUserService userService)
	{
		control = new MultipleFinderControl(userService);

		ok = new JButton(CurrentLocale.get("com.tle.admin.ok"));
		cancel = new JButton(CurrentLocale.get("com.tle.admin.cancel"));

		ok.addActionListener(this);
		cancel.addActionListener(this);

		final int height1 = ok.getPreferredSize().height;
		final int width1 = cancel.getPreferredSize().width;

		final int[] rows = {TableLayout.FILL, height1,};
		final int[] cols = {TableLayout.FILL, width1, width1,};

		all = new JPanel(new TableLayout(rows, cols));
		all.setBorder(AppletGuiUtils.DEFAULT_BORDER);

		all.add(control, new Rectangle(0, 0, 3, 1));
		all.add(ok, new Rectangle(1, 1, 1, 1));
		all.add(cancel, new Rectangle(2, 1, 1, 1));
	}

	public List<String> editExpressions(Component parent, List<String> existingExpressions)
	{
		control.load(existingExpressions);

		dialog = ComponentHelper.createJDialog(parent);
		dialog.setTitle(CurrentLocale.get("com.tle.admin.recipients.multiplefinderdialog.select"));
		dialog.setContentPane(all);
		dialog.setModal(true);

		ComponentHelper.percentageOfScreen(dialog, 0.8f, 0.8f);
		ComponentHelper.centreOnScreen(dialog);

		// Show modal dialog...
		dialog.setVisible(true);
		// Dialog closed.

		if( returnResults )
		{
			return control.save();
		}
		else
		{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == ok )
		{
			destroyDialog();
			returnResults = true;
		}
		else if( e.getSource() == cancel )
		{
			destroyDialog();
			returnResults = false;
		}
	}

	private void destroyDialog()
	{
		dialog.setVisible(false);
		dialog = null;
	}
}
