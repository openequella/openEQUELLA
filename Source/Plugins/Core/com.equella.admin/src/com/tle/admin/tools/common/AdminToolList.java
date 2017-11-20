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

package com.tle.admin.tools.common;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.gui.JHoverButton;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.AdminTool;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.EditAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.RefreshAction;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.client.gui.popup.ListDoubleClickListener;
import com.tle.client.gui.popup.ListPopupListener;
import com.tle.common.NameValue;
import com.tle.common.gui.models.GenericListModel;
import com.tle.common.i18n.CurrentLocale;

public abstract class AdminToolList extends AdminTool
{
	private static final Log LOGGER = LogFactory.getLog(AdminToolList.class);
	private static final NameValue DOWNLOADING_LIST_ELEMENT = new NameValue(
		CurrentLocale.get("com.dytech.edge.admin.guiadmintoollist.downloading"), null); //$NON-NLS-1$

	private boolean listFilled;
	private final List<TLEAction> actions;

	private GenericListModel<NameValue> model;
	private JList<NameValue> list;

	private JPanel everything;

	public AdminToolList()
	{
		actions = new ArrayList<TLEAction>();
	}

	@Override
	public void setup(Set<String> grantedPrivileges, String name)
	{
		getButtonActions(actions);

		listFilled = false;
		model = new GenericListModel<NameValue>();

		list = new JList<>(model);
		list.addMouseListener(new ListDoubleClickListener(list, editAction));
		list.addMouseListener(new ListPopupListener(list, actions));
		list.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				updateButtons();
			}
		});

		JScrollPane scroll = new JScrollPane(list);

		JLabel heading = new JLabel(name);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
		heading.setHorizontalTextPosition(SwingConstants.CENTER);

		JButton refreshButton = new JTextlessButton(refreshAction);
		refreshButton.setBorderPainted(false);
		refreshButton.setIconTextGap(0);
		refreshButton.setBorder(null);

		JComponent buttonsComp = createButtonsPanel();

		final int width1 = refreshButton.getPreferredSize().width;
		final int height1 = refreshButton.getPreferredSize().height;
		final int height2 = buttonsComp.getPreferredSize().height;

		final int[] rows = {5, height1, 5, TableLayout.FILL, 5, height2,};
		final int[] cols = {width1, TableLayout.FILL, width1, 5};

		everything = new JPanel(new TableLayout(rows, cols, 0, 0));
		everything.add(heading, new Rectangle(1, 1, 1, 1));
		everything.add(refreshButton, new Rectangle(2, 1, 1, 1));
		everything.add(scroll, new Rectangle(0, 3, 3, 1));
		everything.add(buttonsComp, new Rectangle(0, 5, 3, 1));

		updateButtons();
	}

	protected void getButtonActions(List<TLEAction> actions)
	{
		actions.add(addAction);
		actions.add(editAction);
		actions.add(removeAction);
	}

	protected JPanel createButtonsPanel()
	{
		List<JButton> buttons = createButtons();

		int count = buttons.size();
		int maxPerRow = 3;

		int cols = count < maxPerRow ? count : maxPerRow;
		int rows = (int) Math.ceil((double) count / (double) maxPerRow);

		JPanel content = new JPanel(new GridLayout(rows, cols));
		for( JButton button : buttons )
		{
			content.add(button);
		}

		return content;
	}

	protected List<JButton> createButtons()
	{
		List<JButton> buttons = new ArrayList<JButton>();
		for( TLEAction action : actions )
		{
			JButton button = new JHoverButton(action);
			button.setBorderPainted(false);
			button.setIconTextGap(5);

			buttons.add(button);
		}
		return buttons;
	}

	protected void updateButtons()
	{
		for( TLEAction action : actions )
		{
			action.update();
		}
	}

	@Override
	public void toolSelected()
	{
		managementPanel.add(everything);
		if( !listFilled )
		{
			listFilled = true;
			addToList(DOWNLOADING_LIST_ELEMENT);
			GlassSwingWorker<?> worker = new GlassSwingWorker<Collection<NameValue>>()
			{
				@Override
				public Collection<NameValue> construct() throws Exception
				{
					return fillList();
				}

				@Override
				protected void afterFinished()
				{
					removeFromList(DOWNLOADING_LIST_ELEMENT);
					addToList(get());
				}

				@Override
				public void exception()
				{
					LOGGER.error("Error", getException());
					removeFromList(DOWNLOADING_LIST_ELEMENT);
					Driver.displayError(parentFrame,
						CurrentLocale.get("com.dytech.edge.admin.guiadmintoollist.error"), getException()); //$NON-NLS-1$
				}
			};
			worker.setComponent(parentFrame);
			worker.start();
		}
	}

	/**
	 * Fill the list
	 */
	protected abstract Collection<NameValue> fillList();

	/**
	 * Called when the add button is pressed.
	 */
	protected abstract void onAdd();

	/**
	 * Called when the edit button is pressed.
	 */
	protected abstract void onEdit();

	/**
	 * Called when the delete button is pressed.
	 */
	protected abstract void onRemove();

	/**
	 * @return The currently selected Object from the list.
	 */
	protected List<NameValue> getSelectedObjects()
	{
		return list.getSelectedValuesList();
	}

	/**
	 * Removes the selected Object from the list.
	 */
	protected void replaceSelectedObject(NameValue value)
	{
		if( !list.isSelectionEmpty() )
		{
			final int index = list.getSelectedIndex();
			model.remove(index);
			model.add(index, value);
		}
	}

	/**
	 * Removes the selected Object from the list.
	 */
	public void removeSelectedObjects()
	{
		int[] selections = list.getSelectedIndices();
		for( int i = selections.length - 1; i >= 0; i-- )
		{
			model.remove(selections[i]);
		}
	}

	/**
	 * Removes the given Object from the list.
	 */
	protected void removeFromList(NameValue obj)
	{
		model.remove(obj);
	}

	/**
	 * Adds all of the given Objects to the list.
	 */
	protected void addToList(Collection<NameValue> c)
	{
		for( NameValue nv : c )
		{
			model.add(nv);
		}
		sortList();
	}

	public void addToList(NameValue object)
	{
		model.add(object);
		sortList();

		list.setSelectedValue(object, true);
	}

	private void sortList()
	{
		List<NameValue> nvs = new ArrayList<NameValue>(model);
		Collections.sort(nvs, new NumberStringComparator<NameValue>());

		model.clear();
		model.addAll(nvs);
	}

	protected TLEAction addAction = new AddAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			onAdd();
		}
	};

	protected TLEAction editAction = new EditAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			onEdit();
		}

		@Override
		public void update()
		{
			setEnabled(list.getSelectedIndices().length == 1);
		}
	};

	protected TLEAction removeAction = new RemoveAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			List<NameValue> selections = getSelectedObjects();

			String message = null;
			if( selections.size() == 1 )
			{
				message = CurrentLocale.get("com.dytech.edge.admin.guiadmintoollist.deleteitem", //$NON-NLS-1$
					selections.get(0).toString());
			}
			else
			{
				message = CurrentLocale.get("com.dytech.edge.admin.guiadmintoollist.deleteitems", //$NON-NLS-1$
					selections.size());
			}

			final int result = JOptionPane.showConfirmDialog(parentFrame, message,
				CurrentLocale.get("com.dytech.edge.admin.guiadmintoollist.confirm"), JOptionPane.YES_NO_OPTION, //$NON-NLS-1$
				JOptionPane.QUESTION_MESSAGE);
			if( result == JOptionPane.YES_OPTION )
			{
				onRemove();
			}
		}

		@Override
		public void update()
		{
			setEnabled(!list.isSelectionEmpty());
		}
	};

	private final TLEAction refreshAction = new RefreshAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			refreshAndSelect();
		}
	};

	public void refresh()
	{
		listFilled = false;
		model.clear();
	}

	public void refreshAndSelect()
	{
		refresh();
		toolSelected();
	}
}
