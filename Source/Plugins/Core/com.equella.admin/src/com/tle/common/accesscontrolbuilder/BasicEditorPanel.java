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

package com.tle.common.accesscontrolbuilder;

import static com.tle.common.security.SecurityConstants.LOGGED_IN_USER_ROLE_ID;
import static com.tle.common.security.SecurityConstants.getRecipient;
import static com.tle.common.security.SecurityConstants.Recipient.EVERYONE;
import static com.tle.common.security.SecurityConstants.Recipient.OWNER;
import static com.tle.common.security.SecurityConstants.Recipient.ROLE;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.dytech.gui.TableLayout;
import com.dytech.gui.VerticalFlowLayout;
import com.tle.common.accesscontrolbuilder.BasicEditor.Mode;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.MultipleFinderDialog;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class BasicEditorPanel extends JComponent implements ItemListener, ActionListener
{
	private static final long serialVersionUID = 1L;
	private final BasicEditor editor;

	private final Node privNode;
	private final PrivilegeList privList;

	private JRadioButton justTheOwner;
	private JRadioButton everyone;
	private JRadioButton everyoneButGuests;
	private JRadioButton limitedSet;

	private LimitedSetListModel listModel;
	private JList list;
	private JScrollPane listScroller;
	private JButton select;
	private final RemoteUserService userService;

	public BasicEditorPanel(BasicEditor editor, RemoteUserService userService, Node privNode, PrivilegeList privList)
	{
		this.editor = editor;
		this.userService = userService;
		this.privNode = privNode;
		this.privList = privList;

		setupGUI(privNode);

		setupInitialButtons(editor, privNode);
	}

	@SuppressWarnings("nls")
	private void setupGUI(Node privNode)
	{
		JPanel options = createOptions(privNode);

		listModel = new LimitedSetListModel();
		list = new JList(listModel);
		list.setCellRenderer(new ExpressionListCellRenderer(userService)
		{
			@Override
			public String getExpression(Object value)
			{
				return ((PrivilegeListEntry) value).getWho();
			}
		});

		listScroller = new JScrollPane(list);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		select = new JButton(CurrentLocale.get("com.tle.admin.security.editors.basiceditorpanel.select"));
		select.addActionListener(this);

		final int height1 = options.getPreferredSize().height;
		final int height2 = select.getPreferredSize().height;
		final int width1 = select.getPreferredSize().width;

		final int[] rows = {height1, TableLayout.FILL, height2,};
		final int[] cols = {20, TableLayout.FILL, width1, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		add(options, new Rectangle(0, 0, 4, 1));
		add(listScroller, new Rectangle(1, 1, 2, 1));
		add(select, new Rectangle(2, 2, 1, 1));
	}

	private JPanel createOptions(Node privNode)
	{
		JPanel options = new JPanel(new VerticalFlowLayout(true, false));
		ButtonGroup buttonGroup = new ButtonGroup();
		if( !PrivilegeTree.isOverrideDefault(privNode, privList.getPrivilege()) )
		{
			justTheOwner = createButton(options, buttonGroup, "security.editor.basic.justTheOwner"); //$NON-NLS-1$
		}
		everyone = createButton(options, buttonGroup, "security.editor.basic.everyone"); //$NON-NLS-1$
		everyoneButGuests = createButton(options, buttonGroup, "security.editor.basic.everyoneButGuests"); //$NON-NLS-1$
		limitedSet = createButton(options, buttonGroup, "security.editor.basic.limitedSet"); //$NON-NLS-1$

		return options;
	}

	private JRadioButton createButton(JPanel panel, ButtonGroup group, String textKey)
	{
		JRadioButton button = new JRadioButton(CurrentLocale.get(textKey));
		button.addItemListener(this);
		group.add(button);
		panel.add(button);
		return button;
	}

	private void setupInitialButtons(BasicEditor editor, Node privNode)
	{
		Mode mode = editor.getModeForPrivilegeList(privNode, privList);
		if( mode == null )
		{
			everyone.setSelected(true);
			modifyPrivilegesToEveryone();
		}
		else
		{
			switch( mode )
			{
				case JUST_THE_OWNER:
					justTheOwner.setSelected(true);
					break;
				case EVERYONE:
					everyone.setSelected(true);
					break;
				case EVERYONE_BUT_GUESTS:
					everyoneButGuests.setSelected(true);
					break;
				case LIMITED_SET:
					limitedSet.setSelected(true);
					break;
				default:
					throw new RuntimeException();
			}
		}

		updateGUI();
	}

	private void updateGUI()
	{
		if( limitedSet != null )
		{
			boolean enabled = limitedSet.isSelected();
			listScroller.setEnabled(enabled);
			list.setEnabled(enabled);
			select.setEnabled(enabled);
			listModel.setEnabled(enabled);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		if( e.getSource() == justTheOwner )
		{
			modifyPrivilegesToJustOwner();
		}
		else if( e.getSource() == everyone )
		{
			modifyPrivilegesToEveryone();
		}
		else if( e.getSource() == everyoneButGuests )
		{
			modifyPrivilegesToEveryoneButGuests();
		}
		else if( e.getSource() == limitedSet )
		{
			modifyPrivilegesToLimitedSet();
		}
		updateGUI();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == select )
		{
			List<String> expressions = listModel.getExpressions();

			MultipleFinderDialog builder = new MultipleFinderDialog(userService);
			expressions = builder.editExpressions(this, expressions);

			if( expressions != null )
			{
				listModel.setExpressions(expressions);
			}
		}
		updateGUI();
	}

	private void modifyPrivilegesToLimitedSet()
	{
		if( editor.getModeForPrivilegeList(privNode, privList) == Mode.LIMITED_SET )
		{
			listModel.reload();
		}
		else
		{
			setSingleEntryOnly(createFinalRevoke());
		}
	}

	private void modifyPrivilegesToEveryoneButGuests()
	{
		PrivilegeListEntry entry = new PrivilegeListEntry();
		entry.setGranted(true);
		entry.setWho(getRecipient(ROLE, LOGGED_IN_USER_ROLE_ID));
		setSingleEntryOnly(entry);
	}

	private void modifyPrivilegesToJustOwner()
	{
		PrivilegeListEntry entry = new PrivilegeListEntry();
		entry.setGranted(true);
		entry.setWho(getRecipient(OWNER));
		setSingleEntryOnly(entry);
	}

	private void modifyPrivilegesToEveryone()
	{
		PrivilegeListEntry entry = new PrivilegeListEntry();
		entry.setGranted(true);
		entry.setWho(getRecipient(EVERYONE));
		setSingleEntryOnly(entry);
	}

	private void setSingleEntryOnly(PrivilegeListEntry entry)
	{
		List<PrivilegeListEntry> entries = getEntries();
		entries.clear();
		entries.add(entry);
	}

	private List<PrivilegeListEntry> getEntries()
	{
		List<PrivilegeListEntry> entries = privList.getEntries();
		if( entries == null )
		{
			entries = new ArrayList<PrivilegeListEntry>();
			privList.setEntries(entries);
		}
		return entries;
	}

	public static PrivilegeListEntry createFinalRevoke()
	{
		PrivilegeListEntry entry = new PrivilegeListEntry();
		entry.setGranted(false);
		entry.setWho(getRecipient(EVERYONE));
		return entry;
	}

	/**
	 * The last entry is special - it's the "revoke everyone" entry. All entries
	 * before it must be grants.
	 * 
	 * @author Nicholas Read
	 */
	private class LimitedSetListModel extends AbstractListModel
	{
		private static final long serialVersionUID = 1L;
		private List<PrivilegeListEntry> entries;
		private boolean enabled = false;

		public LimitedSetListModel()
		{
			super();
		}

		public void setEnabled(boolean enabled)
		{
			if( this.enabled != enabled )
			{
				this.enabled = enabled;
				if( enabled )
				{
					entries = privList.getEntries();
					fireAsIfAllEntriesHaveJustBeenAdded();
				}
				else
				{
					removeAllEntries();
				}
			}
		}

		private void fireAsIfAllEntriesHaveJustBeenAdded()
		{
			int last = entries.size() - 2;
			if( last >= 0 )
			{
				fireIntervalAdded(this, 0, last);
			}
		}

		private void removeAllEntries()
		{
			if( entries != null )
			{
				int last = entries.size() - 2;
				entries = null;
				if( last >= 0 )
				{
					fireIntervalRemoved(this, 0, last);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize()
		{
			return entries == null ? 0 : entries.size() - 1;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public Object getElementAt(int index)
		{
			return entries.get(index);
		}

		public List<String> getExpressions()
		{
			List<String> results = new ArrayList<String>();
			for( Iterator<PrivilegeListEntry> iter = entries.iterator(); iter.hasNext(); )
			{
				PrivilegeListEntry entry = iter.next();
				if( iter.hasNext() )
				{
					results.add(entry.getWho());
				}
			}
			return results;
		}

		public void reload()
		{
			entries = privList.getEntries();
			fireAsIfAllEntriesHaveJustBeenAdded();
		}

		public void setExpressions(List<String> expressions)
		{
			removeAllEntries();

			entries = privList.getEntries();
			entries.clear();
			for( String expression : expressions )
			{
				entries.add(new PrivilegeListEntry(true, expression, false));
			}
			entries.add(createFinalRevoke());
			fireAsIfAllEntriesHaveJustBeenAdded();
		}
	}
}
