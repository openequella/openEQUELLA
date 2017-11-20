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

package com.tle.admin.schema;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.dytech.gui.Changeable;
import com.dytech.gui.TableLayout;
import com.tle.admin.helper.GeneralDialog;
import com.tle.common.gui.models.GenericListModel;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class MultiTargetChooser extends TargetChooser implements ActionListener, Changeable
{
	private static final long serialVersionUID = 1L;

	private JButton add;
	private JButton remove;
	private JList list;
	private GenericListModel<String> listModel;
	private boolean changed;

	public MultiTargetChooser(SchemaModel model, String targetBase)
	{
		super(model, targetBase);
		createGUI();
		clearChanges();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#hasDetectedChanges()
	 */
	@Override
	public boolean hasDetectedChanges()
	{
		return changed;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#clearChanges()
	 */
	@Override
	public void clearChanges()
	{
		changed = false;
	}

	public List<String> getTargets()
	{
		return new ArrayList<String>(listModel);
	}

	public void setTargets(List<String> targets)
	{
		listModel.clear();
		listModel.addAll(targets);
		clearChanges();
	}

	private void createGUI()
	{
		add = new JButton(CurrentLocale.get("com.tle.admin.add")); //$NON-NLS-1$
		remove = new JButton(CurrentLocale.get("com.tle.admin.remove")); //$NON-NLS-1$

		add.addActionListener(this);
		remove.addActionListener(this);

		listModel = new GenericListModel<String>();
		list = new JList(listModel);
		JScrollPane scroller = new JScrollPane(list);

		final int height = remove.getPreferredSize().height;
		final int width = remove.getPreferredSize().width;
		final int[] rows = {height, height,};
		final int[] columns = {width, TableLayout.FILL,};

		setLayout(new TableLayout(rows, columns, 5, 5));

		add(add, new Rectangle(0, 0, 1, 1));
		add(remove, new Rectangle(0, 1, 1, 1));
		add(scroller, new Rectangle(1, 0, 1, 2));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == add )
		{
			WhereTargetDialog dialog = new WhereTargetDialog(this, getTree(), warnAboutNonFields);
			dialog.setNonLeafSelection(enableNonLeafSelection);
			dialog.setAttributesAllowed(attributesAllowed);
			dialog.showDialog();

			if( dialog.getResponse() == GeneralDialog.OK_RESPONSE )
			{
				SchemaNode node = (SchemaNode) dialog.getValue();
				if( node != null )
				{
					changed = true;

					String fullxpath = node.getXmlPath();
					String xpath = fullxpath;
					if( getTargetBase() != null )
					{
						xpath = xpath.substring(getTargetBase().length());
					}

					if( xpath.length() == 0 )
					{
						xpath = "/"; //$NON-NLS-1$
					}

					listModel.add(xpath);
					list.updateUI();
					// This needs to be full path to avoid 'spurious' errors
					// with repeater children
					fireTargedAdded(fullxpath);
				}
			}
		}
		else if( e.getSource() == remove )
		{
			final int indices[] = list.getSelectedIndices();
			if( indices.length > 0 )
			{
				changed = true;
				for( int i = indices.length; i > 0; i-- )
				{
					String target = listModel.remove(indices[i - 1]);
					fireTargedRemoved(target);
				}
			}
			list.updateUI();
		}
	}
}
