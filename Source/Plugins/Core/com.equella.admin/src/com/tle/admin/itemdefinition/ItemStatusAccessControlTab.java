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

package com.tle.admin.itemdefinition;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.gui.Changeable;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.common.WorkAroundChucksStupidHacksAdapater;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemStatus;
import com.tle.common.EntityPack;
import com.tle.common.accesscontrolbuilder.AccessEditor;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.ItemStatusTarget;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class ItemStatusAccessControlTab extends AbstractItemdefTab implements Changeable
{
	private JComboBox statuses;
	private AccessEditor editor;

	private Map<ItemStatusTarget, TargetList> targetLists;
	private Map<ItemStatusTarget, TargetList> originalTargetLists;

	private Map<ItemStatus, ItemStatusTarget> mapping;
	private ItemStatus currentSelection;

	public ItemStatusAccessControlTab()
	{
		super();
	}

	@Override
	public void init(Component parent)
	{
		JLabel text = new JLabel(CurrentLocale.get("security.editor.itemstatus")); //$NON-NLS-1$

		statuses = new JComboBox();

		for( ItemStatus status : ItemStatus.values() )
		{
			statuses.addItem(status);
		}

		statuses.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveCurrentSelection();
				loadCurrentSelection();
			}
		});

		editor = new AccessEditor(clientService.getService(RemoteTLEAclManager.class),
			clientService.getService(RemoteUserService.class));

		final int height1 = statuses.getPreferredSize().height;
		final int width1 = text.getPreferredSize().width;

		final int[] rows = {height1, TableLayout.FILL,};
		final int[] cols = {width1, TableLayout.DOUBLE_FILL, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		add(text, new Rectangle(0, 0, 1, 1));
		add(statuses, new Rectangle(1, 0, 1, 1));
		add(editor, new Rectangle(0, 1, 3, 1));
	}

	@Override
	public void validation()
	{
		// Nothing to validate here.
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.itemstatusaccesscontroltab.title"); //$NON-NLS-1$
	}

	@Override
	public void load()
	{
		mapping = new HashMap<ItemStatus, ItemStatusTarget>();
		targetLists = new HashMap<ItemStatusTarget, TargetList>();

		// Load the given target lists
		final EntityPack<ItemDefinition> entityPack = state.getEntityPack();
		if( entityPack.getOtherTargetLists() != null )
		{
			for( Map.Entry<Object, TargetList> entry : entityPack.getOtherTargetLists().entrySet() )
			{
				if( entry.getKey() instanceof ItemStatusTarget )
				{
					ItemStatusTarget target = (ItemStatusTarget) entry.getKey();
					mapping.put(target.getItemStatus(), target);
					targetLists.put(target, entry.getValue());
				}
			}
		}

		// Load in blank target lists for other options
		for( ItemStatus status : ItemStatus.values() )
		{
			if( !mapping.containsKey(status) )
			{
				ItemStatusTarget target = new ItemStatusTarget(status, state.getEntity());
				targetLists.put(target, null);
				mapping.put(status, target);
			}
		}

		statuses.setSelectedItem(0);
		loadCurrentSelection();
		clearChanges();
	}

	@Override
	public void save()
	{
		saveCurrentSelection();

		EntityPack<ItemDefinition> entityPack = state.getEntityPack();
		Map<Object, TargetList> otherTargetLists = entityPack.getOtherTargetLists();
		if( otherTargetLists == null )
		{
			otherTargetLists = new HashMap<Object, TargetList>();
			entityPack.setOtherTargetLists(otherTargetLists);
		}
		else
		{
			for( Iterator<Object> iter = otherTargetLists.keySet().iterator(); iter.hasNext(); )
			{
				if( iter.next() instanceof ItemStatusTarget )
				{
					iter.remove();
				}
			}
		}

		otherTargetLists.putAll(targetLists);
	}

	private void loadCurrentSelection()
	{
		currentSelection = (ItemStatus) statuses.getSelectedItem();
		TargetList list = targetLists.get(mapping.get(currentSelection));

		ItemStatusTarget target = new ItemStatusTarget(currentSelection, state.getEntity());
		editor.load(target, list, Node.ITEM_STATUS);
	}

	private void saveCurrentSelection()
	{
		if( currentSelection != null )
		{
			targetLists.put(mapping.get(currentSelection), editor.save());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#hasDetectedChanges()
	 */
	@Override
	public boolean hasDetectedChanges()
	{
		saveCurrentSelection();
		return !originalTargetLists.equals(targetLists);
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#clearChanges()
	 */
	@Override
	public void clearChanges()
	{
		originalTargetLists = new HashMap<ItemStatusTarget, TargetList>(targetLists);
	}

	@Override
	public JPanel getComponent()
	{
		// TODO: Remove this stupid dodgy hack by removing JChangeDetectorPanel
		// and JFakePanel
		return new WorkAroundChucksStupidHacksAdapater(super.getComponent(), this);
	}
}
