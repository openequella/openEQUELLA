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

package com.tle.admin.schema.manager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.tle.admin.common.gui.EditorHelper;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class PartiallyLockedDialog implements ActionListener
{
	private static final Dimension WINDOW_SIZE = new Dimension(500, 450);

	private JDialog dialog;
	private JPanel content;
	private JButton unlock;
	private JButton close;
	private boolean doUnlock = false;

	public PartiallyLockedDialog(Collection<BaseEntityLabel> usages)
	{
		setup(usages);
	}

	private void setup(Collection<BaseEntityLabel> usages)
	{
		StringBuilder msg = new StringBuilder(
			CurrentLocale.get("com.tle.admin.schema.manager.partiallylockeddialog.beingused")); //$NON-NLS-1$

		for( BaseEntityLabel entity : usages )
		{
			msg.append(" - "); //$NON-NLS-1$
			msg.append(BundleCache.getString(entity));
			msg.append(" ["); //$NON-NLS-1$
			msg.append(CurrentLocale.get("com.tle.admin.schema.manager.partiallylockeddialog." //$NON-NLS-1$
				+ (entity.getPrivType().equals("COLLECTION") ? "collection" : "search"))); //$NON-NLS-1$ //$NON-NLS-2$
			msg.append("]\n"); //$NON-NLS-1$
		}

		msg.append(CurrentLocale.get("com.tle.admin.schema.manager.partiallylockeddialog.info")); //$NON-NLS-1$

		JTextArea message = new JTextArea(msg.toString());
		message.setEditable(false);
		message.setWrapStyleWord(true);
		message.setLineWrap(true);

		JScrollPane scroller = new JScrollPane(message);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		unlock = new JButton(CurrentLocale.get("schema.unlock")); //$NON-NLS-1$
		close = new JButton(EditorHelper.DEFAULT_CLOSE_TEXT);

		unlock.addActionListener(this);
		close.addActionListener(this);

		int height1 = unlock.getPreferredSize().height;
		int width1 = unlock.getPreferredSize().width;

		int[] rows = {TableLayout.FILL, height1};
		int[] cols = {TableLayout.FILL, width1, width1, TableLayout.FILL};

		content = new JPanel(new TableLayout(rows, cols));
		content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		content.add(scroller, new Rectangle(0, 0, 4, 1));
		content.add(unlock, new Rectangle(1, 1, 1, 1));
		content.add(close, new Rectangle(2, 1, 1, 1));
	}

	public boolean askToUnlock(Component parent)
	{
		dialog = ComponentHelper.createJDialog(parent);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.getRootPane().setDefaultButton(close);
		dialog.setTitle(CurrentLocale.get("com.tle.admin.schema.manager.partiallylockeddialog.title")); //$NON-NLS-1$
		dialog.setContentPane(content);
		dialog.setModal(true);

		dialog.setSize(WINDOW_SIZE);
		ComponentHelper.centreOnScreen(dialog);

		dialog.setVisible(true);
		dialog = null;

		return doUnlock;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == close )
		{
			dialog.dispose();
		}
		else if( e.getSource() == unlock )
		{
			int result = JOptionPane.showConfirmDialog(dialog,
				CurrentLocale.get("schema.prompt.unlock.body"), CurrentLocale.get("schema.prompt.unlock.title"), //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.YES_NO_OPTION);

			if( result == JOptionPane.YES_OPTION )
			{
				doUnlock = true;
				dialog.dispose();
			}
		}
	}
}
