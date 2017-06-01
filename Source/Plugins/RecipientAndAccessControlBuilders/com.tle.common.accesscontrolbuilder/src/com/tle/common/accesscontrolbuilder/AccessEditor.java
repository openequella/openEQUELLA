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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.gui.Changeable;
import com.dytech.gui.TableLayout;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class AccessEditor extends JPanel implements ActionListener, Changeable
{
	private static final long serialVersionUID = 1L;
	private Map<String, PrivilegeList> privilegeToList;
	private Map<AbstractButton, PrivilegeListEditor> editors;
	private AbstractButton lastSelected;
	private boolean ignoreActionEvents;

	private JComboBox<NameValue> actions;
	private JRadioButton basic;
	private JRadioButton advanced;
	private JRadioButton inherited;
	private JPanel container;

	private Object domainObj;
	private Node privilegeNode;
	private TargetList originaList;

	public AccessEditor(RemoteTLEAclManager aclManager, RemoteUserService userService)
	{
		setupGUI();

		editors = new LinkedHashMap<AbstractButton, PrivilegeListEditor>();
		editors.put(inherited, new InheritedEditor(aclManager, userService));
		editors.put(basic, new BasicEditor(userService));
		editors.put(advanced, new AdvancedEditor(aclManager, userService));
	}

	public void load(Object domainObj, TargetList targetList, Node privilegeNode)
	{
		this.domainObj = domainObj;
		this.privilegeNode = privilegeNode;
		this.originaList = targetList;

		privilegeToList = new HashMap<String, PrivilegeList>();
		if( targetList != null )
		{
			for( TargetListEntry entry : targetList.getEntries() )
			{
				PrivilegeListEntry ple = new PrivilegeListEntry();
				ple.setGranted(entry.isGranted());
				ple.setOverride(entry.isOverride());
				ple.setWho(entry.getWho());

				String privilege = entry.getPrivilege();
				PrivilegeList list = privilegeToList.get(privilege);
				if( list == null )
				{
					list = new PrivilegeList(privilege);
					privilegeToList.put(privilege, list);
				}
				list.getEntries().add(ple);
			}
		}

		// Find the user-friendly message for each privilege
		Set<NameValue> nameAndPrivilege = new TreeSet<NameValue>(new NumberStringComparator<NameValue>());

		Map<String, Integer> allPrivileges = PrivilegeTree.getAllPrivilegesForNode(privilegeNode);
		for( String privilege : allPrivileges.keySet() )
		{
			String nodeText = privilegeNode.toString();
			String text = CurrentLocale.get("security.privilege." + nodeText + '.' + privilege);
			// PURGE_ITEM can only be used on deleted items
			if( !(privilegeNode.equals(Node.ITEM_STATUS) && privilege.equals("PURGE_ITEM")) )
			{
				nameAndPrivilege.add(new NameValue(text, privilege));
			}
		}

		try
		{
			ignoreActionEvents = true;
			actions.removeAllItems();
			for( NameValue nv : nameAndPrivilege )
			{
				actions.addItem(nv);
			}
		}
		finally
		{
			ignoreActionEvents = false;
		}
		actions.setSelectedIndex(0);
	}

	public TargetList save()
	{
		List<TargetListEntry> entries = new ArrayList<TargetListEntry>();

		for( PrivilegeList list : privilegeToList.values() )
		{
			String privilege = list.getPrivilege();
			for( PrivilegeListEntry entry : list.getEntries() )
			{
				TargetListEntry tle = new TargetListEntry();
				tle.setGranted(entry.isGranted());
				tle.setOverride(entry.isOverride());
				tle.setWho(entry.getWho());
				tle.setPrivilege(privilege);

				entries.add(tle);
			}
		}

		TargetList list = new TargetList();
		list.setEntries(entries);
		return list;
	}

	@Override
	public void clearChanges()
	{
		this.originaList = save();
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return !save().equals(originaList);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == actions )
		{
			if( !ignoreActionEvents )
			{
				switchPrivilegeLists();
			}
		}
		else
		{
			switchModes((AbstractButton) e.getSource());
		}
	}

	private void switchPrivilegeLists()
	{
		PrivilegeList privList = getCurrentPrivilegeList();
		Entry<AbstractButton, PrivilegeListEditor> editor = getFirstSuitableEditor(privList);

		lastSelected = editor.getKey();
		lastSelected.setSelected(true);

		JComponent component2 = editor.getValue().createView(domainObj, privilegeNode, privList);
		setContainer(component2);
	}

	private void switchModes(AbstractButton newSelection)
	{
		PrivilegeListEditor editor = editors.get(newSelection);
		PrivilegeList privList = getCurrentPrivilegeList();

		if( !editor.canHandle(privilegeNode, privList) )
		{
			String message = CurrentLocale.get("security.editor.mayLoseEntries");
			int result = JOptionPane.showConfirmDialog(this, message, null, JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if( result == JOptionPane.NO_OPTION )
			{
				lastSelected.setSelected(true);
				return;
			}
		}

		lastSelected = newSelection;
		setContainer(editor.createView(domainObj, privilegeNode, privList));
	}

	private void setContainer(JComponent component)
	{
		container.removeAll();
		if( component != null )
		{
			container.add(component);
		}
		container.updateUI();
	}

	private PrivilegeList getCurrentPrivilegeList()
	{
		NameValue namePrivilege = (NameValue) actions.getSelectedItem();
		PrivilegeList list = privilegeToList.get(namePrivilege.getValue());
		if( list == null )
		{
			list = new PrivilegeList(namePrivilege.getValue());
			privilegeToList.put(namePrivilege.getValue(), list);
		}
		return list;
	}

	private Map.Entry<AbstractButton, PrivilegeListEditor> getFirstSuitableEditor(PrivilegeList list)
	{
		for( Map.Entry<AbstractButton, PrivilegeListEditor> entry : editors.entrySet() )
		{
			if( entry.getValue().canHandle(privilegeNode, list) )
			{
				return entry;
			}
		}

		throw new RuntimeException("We should always have at least one editor that can handle anything");
	}

	// //// SETUP AND GUI CREATION ////////////////////////////////////////////

	private void setupGUI()
	{
		JComponent whoCanPanel = createWhoCanPanel();
		JComponent modePanel = createModePanel();

		container = new JPanel(new GridLayout(1, 1));

		JSeparator separator = new JSeparator();

		final int height1 = whoCanPanel.getPreferredSize().height;
		final int height2 = modePanel.getMinimumSize().height;
		final int height3 = separator.getPreferredSize().height;
		final int[] rows = {height1, height2, height3, TableLayout.FILL,};
		final int[] cols = {TableLayout.DOUBLE_FILL, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		add(whoCanPanel, new Rectangle(0, 0, 1, 1));
		add(modePanel, new Rectangle(0, 1, 2, 1));
		add(separator, new Rectangle(0, 2, 2, 1));
		add(container, new Rectangle(0, 3, 2, 1));
	}

	private JComponent createWhoCanPanel()
	{
		JLabel whocanStart = new JLabel(CurrentLocale.get("security.editor.whocan.before"));
		JLabel whocanEnd = new JLabel(CurrentLocale.get("security.editor.whocan.after"));

		actions = new JComboBox<>();
		actions.addActionListener(this);

		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.add(whocanStart, BorderLayout.WEST);
		panel.add(actions, BorderLayout.CENTER);
		panel.add(whocanEnd, BorderLayout.EAST);

		return panel;
	}

	private JComponent createModePanel()
	{
		basic = new JRadioButton(CurrentLocale.get("security.editor.mode.basic"));
		advanced = new JRadioButton(CurrentLocale.get("security.editor.mode.advanced"));
		inherited = new JRadioButton(CurrentLocale.get("security.editor.mode.inherited"));

		basic.addActionListener(this);
		advanced.addActionListener(this);
		inherited.addActionListener(this);

		ButtonGroup radiogroup = new ButtonGroup();
		radiogroup.add(basic);
		radiogroup.add(advanced);
		radiogroup.add(inherited);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(basic);
		panel.add(advanced);
		panel.add(inherited);

		return panel;
	}
}
