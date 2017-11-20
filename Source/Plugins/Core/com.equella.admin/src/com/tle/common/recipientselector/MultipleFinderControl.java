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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.EventListenerList;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.tle.common.gui.models.GenericListModel;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class MultipleFinderControl extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private final RemoteUserService userService;
	private final RecipientFilter[] filters;
	private final EventListenerList listeners;

	private JButton addSelected;
	private JButton removeSelected;
	private JButton removeAll;

	private GenericListModel<String> listModel;
	private JList list;
	private UserGroupRoleFinder finder;

	public MultipleFinderControl(RemoteUserService userService)
	{
		this(userService, RecipientFilter.USERS, RecipientFilter.GROUPS, RecipientFilter.ROLES);
	}

	public MultipleFinderControl(RemoteUserService userService, RecipientFilter... filters)
	{
		this.userService = userService;
		this.filters = filters;

		listeners = new EventListenerList();

		setupGUI();
	}

	@Override
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);

		finder.setEnabled(b);
		list.setEnabled(b);

		addSelected.setEnabled(b);
		removeSelected.setEnabled(b);
		removeAll.setEnabled(b);
	}

	public void watch(ChangeDetector detector)
	{
		detector.watch(listModel);
	}

	@SuppressWarnings("nls")
	private void setupGUI()
	{
		addSelected = new JButton(">");
		removeSelected = new JButton("<");
		removeAll = new JButton("<<");

		addSelected.addActionListener(this);
		removeSelected.addActionListener(this);
		removeAll.addActionListener(this);

		finder = new TabbedFinder(userService, filters);

		listModel = new GenericListModel<String>();

		list = new JList(listModel);
		list.setCellRenderer(new ExpressionListCellRenderer(userService));

		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		final int height1 = addSelected.getPreferredSize().height;
		final int width1 = removeAll.getPreferredSize().width;
		final int width2 = 200;

		final int[] rows = {TableLayout.FILL, height1, height1, height1, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL, width1, width2,};

		setLayout(new TableLayout(rows, cols));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add((Component) finder, new Rectangle(0, 0, 1, 5));
		add(addSelected, new Rectangle(1, 1, 1, 1));
		add(removeSelected, new Rectangle(1, 2, 1, 1));
		add(removeAll, new Rectangle(1, 3, 1, 1));
		add(listScroller, new Rectangle(2, 0, 1, 5));
	}

	public void load(List<String> selections)
	{
		listModel.clear();
		if( selections != null )
		{
			listModel.addAll(selections);
		}
	}

	public List<String> save()
	{
		return new ArrayList<String>(listModel);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == addSelected )
		{
			addFinderResultsToList();
			raiseActionListenerEvent();
		}
		else if( e.getSource() == removeSelected )
		{
			listModel.removeAll(list.getSelectedValues());
			raiseActionListenerEvent();
		}
		else if( e.getSource() == removeAll )
		{
			listModel.clear();
			raiseActionListenerEvent();
		}
	}

	private void addFinderResultsToList()
	{
		RecipientFilter selectedFilter = finder.getSelectedFilter();
		for( Object obj : finder.getSelectedResults() )
		{
			String string = RecipientUtils.convertToRecipient(selectedFilter, obj);
			if( !listModel.contains(string) )
			{
				listModel.add(string);
			}
		}
	}

	public void addActionListener(ActionListener listener)
	{
		listeners.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener)
	{
		listeners.remove(ActionListener.class, listener);
	}

	private void raiseActionListenerEvent()
	{
		ActionEvent event = new ActionEvent(this, 0, null);
		for( ActionListener l : listeners.getListeners(ActionListener.class) )
		{
			l.actionPerformed(event);
		}
	}
}
