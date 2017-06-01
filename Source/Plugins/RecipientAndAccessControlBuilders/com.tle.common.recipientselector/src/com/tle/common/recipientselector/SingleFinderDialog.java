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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class SingleFinderDialog implements ActionListener, FinderListener
{
	private JButton ok;
	private JButton cancel;

	private UserGroupRoleFinder finder;

	private JPanel all;
	private JDialog dialog;
	private Object result;

	/**
	 * Creates with the given finder.
	 */
	public SingleFinderDialog(UserGroupRoleFinder finder)
	{
		this.finder = finder;
		setupGUI();
	}

	/**
	 * Creates a default tabbed finder.
	 */
	public SingleFinderDialog(RemoteUserService userService, RecipientFilter... filters)
	{
		this(new TabbedFinder(userService, filters));
	}

	private void setupGUI()
	{
		finder.setSingleSelectionOnly(true);
		finder.addFinderListener(this);

		ok = new JButton(CurrentLocale.get("com.tle.admin.ok"));
		cancel = new JButton(CurrentLocale.get("com.tle.admin.cancel"));

		ok.addActionListener(this);
		cancel.addActionListener(this);

		ok.setEnabled(false);

		final int height1 = ok.getPreferredSize().height;
		final int width1 = cancel.getPreferredSize().width;

		final int[] rows = {TableLayout.FILL, height1,};
		final int[] cols = {TableLayout.FILL, width1, width1,};

		all = new JPanel(new TableLayout(rows, cols));
		all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		all.add((Component) finder, new Rectangle(0, 0, 3, 1));
		all.add(ok, new Rectangle(1, 1, 1, 1));
		all.add(cancel, new Rectangle(2, 1, 1, 1));
	}

	public void clearAll()
	{
		finder.clearAll();
	}

	public Pair<RecipientFilter, Object> showFinder(Component parent)
	{
		resetState();

		dialog = ComponentHelper.createJDialog(parent);
		dialog.setTitle(CurrentLocale.get("com.tle.admin.recipients.singlefinderdialog.title"));
		dialog.setContentPane(all);
		dialog.setModal(true);

		ComponentHelper.percentageOfScreen(dialog, 0.8f, 0.8f);
		ComponentHelper.centreOnScreen(dialog);

		// Show modal dialog...
		dialog.setVisible(true);
		// Dialog closed.

		Pair<RecipientFilter, Object> pair = null;
		if( result != null )
		{
			pair = new Pair<RecipientFilter, Object>();
			pair.setFirst(finder.getSelectedFilter());
			pair.setSecond(result);
		}
		return pair;
	}

	private void resetState()
	{
		result = null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.recipients.FinderListener#valueChanged(com.tle.admin.recipients
	 * .FinderEvent)
	 */
	@Override
	public void valueChanged(FinderEvent e)
	{
		ok.setEnabled(e.getSelectionCount() > 0);
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
			try
			{
				result = finder.getSelectedResults().get(0);
				destroyDialog();
			}
			catch( RuntimeApplicationException ex )
			{
				JOptionPane.showMessageDialog(dialog, CurrentLocale.get("com.tle.finder.invalid.expression"), "Error",
					JOptionPane.WARNING_MESSAGE);
			}
		}
		else if( e.getSource() == cancel )
		{
			destroyDialog();
		}
	}

	private void destroyDialog()
	{
		dialog.setVisible(false);
		dialog = null;
	}
}
