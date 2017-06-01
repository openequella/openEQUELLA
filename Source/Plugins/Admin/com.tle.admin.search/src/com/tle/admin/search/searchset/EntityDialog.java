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

package com.tle.admin.search.searchset;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.dytech.gui.ComponentHelper;
import com.tle.beans.NameId;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.gui.models.GenericListModel;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class EntityDialog implements ActionListener, ListSelectionListener, MouseListener
{
	private JButton okButton;
	private JButton cancelButton;
	private JList list;
	private GenericListModel<NameId> model;
	private String title;

	private JPanel content;
	private JDialog dialog;
	private boolean cancelled = true;

	public EntityDialog(String entityName)
	{
		setup(entityName);
	}

	private void setup(String entityName)
	{
		title = CurrentLocale.get("com.tle.admin.search.entitydialog.select", entityName);

		model = new GenericListModel<NameId>();
		list = new JList(model);
		list.addListSelectionListener(this);
		list.addMouseListener(this);

		JScrollPane scroll = new JScrollPane(list);

		okButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.ok"));
		cancelButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.cancel"));

		okButton.addActionListener(this);
		cancelButton.addActionListener(this);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(okButton);
		buttons.add(cancelButton);

		buttons.setPreferredSize(buttons.getMinimumSize());

		content = new JPanel(new BorderLayout(5, 5));
		content.setBorder(AppletGuiUtils.DEFAULT_BORDER);

		content.add(new JLabel(title), BorderLayout.NORTH);
		content.add(scroll, BorderLayout.CENTER);
		content.add(buttons, BorderLayout.SOUTH);

		updateButtons();
	}

	private void updateButtons()
	{
		okButton.setEnabled(!list.isSelectionEmpty());
	}

	public List<NameId> showDialog(Component parent)
	{
		dialog = ComponentHelper.createJDialog(parent);
		dialog.setModal(true);
		dialog.getContentPane().add(content);
		dialog.setTitle(title);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		dialog.setSize(300, 300);
		ComponentHelper.centreOnScreen(dialog);

		dialog.setVisible(true);

		if( cancelled || list.isSelectionEmpty() )
		{
			return Collections.emptyList();
		}
		else
		{
			List<NameId> results = new ArrayList<NameId>();
			for( int i : list.getSelectedIndices() )
			{
				results.add(model.get(i));
			}
			return results;
		}
	}

	private void closeDialog(boolean cancelled)
	{
		this.cancelled = cancelled;
		dialog.dispose();
		dialog = null;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		closeDialog(e.getSource() == cancelButton);
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		updateButtons();
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if( e.getClickCount() == 2 )
		{
			closeDialog(false);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		// We don't care about this event
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		// We don't care about this event
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		// We don't care about this event
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// We don't care about this event
	}

	protected void loadEntities(Collection<NameId> usableEntities, Set<NameId> filterOut)
	{
		for( NameId entity : usableEntities )
		{
			if( !filterOut.contains(entity) )
			{
				model.add(entity);
			}
		}
	}

	/**
	 * @author Nicholas Read
	 */
	public static class ItemDefinitionDialog extends EntityDialog
	{
		public ItemDefinitionDialog(Collection<NameId> values, Set<NameId> filterOut)
		{
			super(CurrentLocale.get("com.tle.admin.search.entitydialog.collection"));
			loadEntities(values, filterOut);
		}
	}

	/**
	 * @author Nicholas Read
	 */
	public static class SchemaDialog extends EntityDialog
	{
		public SchemaDialog(Collection<NameId> values, Set<NameId> filterOut)
		{
			super(CurrentLocale.get("com.tle.admin.search.entitydialog.schema"));
			loadEntities(values, filterOut);
		}
	}

	/**
	 * @author Nicholas Read
	 */
	public static class CourseInfoDialog extends EntityDialog
	{
		public CourseInfoDialog(Collection<NameId> values, Set<NameId> filterOut)
		{
			super(CurrentLocale.get("com.tle.admin.search.entitydialog.course"));
			loadEntities(values, filterOut);
		}
	}
}
