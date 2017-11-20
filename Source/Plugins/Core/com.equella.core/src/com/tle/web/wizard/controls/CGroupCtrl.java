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

package com.tle.web.wizard.controls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.Group;
import com.dytech.edge.wizard.beans.control.GroupItem;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.google.common.collect.ImmutableList;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.core.wizard.WizardPageException;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;

/**
 * Provides a data model for group controls.
 * 
 * @author Nicholas Read
 */
public class CGroupCtrl extends GroupsCtrl
{
	private static final long serialVersionUID = 1L;

	private String checkType;

	public CGroupCtrl(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
		throws WizardPageException
	{
		super(page, controlNumber, nestingLevel, controlBean);
		Group groupBean = (Group) controlBean;
		checkType = groupBean.getType();

		int index = 0;
		for( GroupItem group : groupBean.getGroups() )
		{
			String name = CurrentLocale.get(group.getName());
			String value = group.getValue();

			if( name.length() == 0 )
			{
				name = value;
			}
			else if( value.length() == 0 )
			{
				value = name;
			}

			Item item = new Item(name, value);

			items.add(item);

			List<HTMLControl> vGroup = new ArrayList<HTMLControl>();
			wizardPage.createCtrls(group.getControls(), getNestingLevel() + 1, vGroup);
			addGroup(new ControlGroup(vGroup, index));
			index++;
		}

		if( checkType == null || checkType.length() == 0 )
		{
			if( getGroupSize() > 1 )
			{
				checkType = "radio"; //$NON-NLS-1$
			}
			else
			{
				checkType = "checkbox"; //$NON-NLS-1$
			}
		}
	}

	@Override
	public void resetToDefaults()
	{
		for( Item item : items )
		{
			item.setSelected(false);
		}
		super.resetToDefaults();
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		super.loadFromDocument(itemxml);

		final ImmutableList<ControlGroup> groups = getGroups();
		int nGroups = getGroupSize();
		for( int i = 0; i < nGroups; i++ )
		{
			Item oItem = items.get(i);
			ControlGroup vGroup = groups.get(i);

			loadGroup(vGroup, itemxml, !oItem.isSelected());
		}
	}

	@Override
	public void saveToDocument(PropBagEx itemxml) throws Exception
	{
		super.saveToDocument(itemxml);

		final ImmutableList<ControlGroup> groups = getGroups();
		int nGroups = getGroupSize();
		for( int i = 0; i < nGroups; i++ )
		{
			ControlGroup vGroup = groups.get(i);
			Item oItem = items.get(i);
			saveGroup(vGroup, itemxml, !oItem.isSelected());
		}
	}

	@Override
	public void validate()
	{
		int i = 0;
		for( Iterator<ControlGroup> iter = getGroups().iterator(); iter.hasNext(); i++ )
		{
			ControlGroup group = iter.next();
			boolean isSelected = getItem(i).isSelected();
			for( HTMLControl control : group.getControls() )
			{
				if( isSelected )
				{
					validate(control);
				}
				control.setDontShowEmpty(!isSelected);
			}
		}
	}

	@Override
	public Label getEmptyMessage()
	{
		return new KeyLabel("wizard.controls.groups.please"); //$NON-NLS-1$
	}

	public String getCheckType()
	{
		return checkType;
	}
}
