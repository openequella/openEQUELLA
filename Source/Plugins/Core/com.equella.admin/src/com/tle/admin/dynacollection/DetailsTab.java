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

package com.tle.admin.dynacollection;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.java.plugin.registry.Extension;

import com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.SingleUserSelector;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class DetailsTab extends BaseEntityTab<DynaCollection> implements AbstractDetailsTab<DynaCollection>
{
	private I18nTextField name;
	private I18nTextArea description;
	private SingleUserSelector owner;
	private final Map<String, JCheckBox> usages = new HashMap<String, JCheckBox>();

	@Override
	public void init(Component parent)
	{
		setupGUI();
	}

	@Override
	public String getTitle()
	{
		return getString("dyncol.detailstab.title");
	}

	private void setupGUI()
	{
		name = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextArea(BundleCache.getLanguages());

		owner = new SingleUserSelector(clientService.getService(RemoteUserService.class));

		setLayout(new MigLayout("wrap 2,fillx", "[align label][fill, grow]25%"));

		add(new JLabel(getString("dyncol.detailstab.name")));
		add(name, "growx");

		add(new JLabel(getString("dyncol.detailstab.desc")), "top");
		add(description, "height pref*3, growx");

		add(new JLabel(getString("dyncol.detailstab.owner")));
		add(owner);

		add(new JLabel(getString("dyncol.detailstab.usages")), "span 2");

		for( Extension ext : pluginService.getConnectedExtensions("com.tle.common.dynacollection", "usages") )
		{
			final String nameKey = ext.getParameter("nameKey").valueAsString();
			final JCheckBox cb = new JCheckBox(CurrentLocale.get(nameKey));

			usages.put(ext.getId(), cb);
			add(cb, "skip");
		}

		// Make sure things are read-only.
		if( state.isReadonly() )
		{
			name.setEnabled(false);
			description.setEnabled(false);
			owner.setEnabled(false);

			for( JCheckBox cb : usages.values() )
			{
				cb.setEnabled(false);
			}
		}
	}

	@Override
	public void addNameListener(KeyListener listener)
	{
		name.addKeyListener(listener);
	}

	@Override
	public void load()
	{
		final DynaCollection dynaCollection = state.getEntity();
		name.load(dynaCollection.getName());
		description.load(dynaCollection.getDescription());
		owner.setUserId(dynaCollection.getOwner());

		final Set<String> usageIds = dynaCollection.getUsageIds();
		if( !Check.isEmpty(usageIds) )
		{
			for( String usageId : usageIds )
			{
				JCheckBox cb = usages.get(usageId);
				if( cb != null )
				{
					cb.setSelected(true);
				}
			}
		}
	}

	@Override
	public void save()
	{
		final DynaCollection dynaCollection = state.getEntity();
		dynaCollection.setName(name.save());
		dynaCollection.setDescription(description.save());
		dynaCollection.setOwner(owner.getUser().getUniqueID());

		final Set<String> usageIds = new HashSet<String>();
		for( Entry<String, JCheckBox> usage : usages.entrySet() )
		{
			if( usage.getValue().isSelected() )
			{
				usageIds.add(usage.getKey());
			}
		}
		dynaCollection.setUsageIds(usageIds);
	}

	@Override
	public void validation() throws EditorException
	{
		if( name.isCompletelyEmpty() )
		{
			throw new EditorException(getString("dyncol.detailstab.supplyname")); //$NON-NLS-1$
		}

		if( owner.getUser() == null )
		{
			throw new EditorException(getString("dyncol.detailstab.noowner")); //$NON-NLS-1$
		}
	}
}
