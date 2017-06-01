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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.tle.annotation.Nullable;
import com.tle.common.NameValue;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.controls.WizardPage;

public abstract class OptionCtrl extends AbstractHTMLControl
{
	private static final long serialVersionUID = 1L;

	protected final List<Item> items = new ArrayList<Item>();
	private boolean empty;

	protected OptionCtrl(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
	}

	public Item getItem(int index)
	{
		return items.get(index);
	}

	public List<Item> getItems()
	{
		return items;
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		TargetNode firstTarget = getFirstTarget();
		if( firstTarget.nodeExists(itemxml, 0) )
		{
			empty = !selectItems(items, firstTarget, itemxml);
		}
	}

	@Override
	public void saveToDocument(PropBagEx itemxml) throws Exception
	{
		clearTargets(itemxml);
		for( Item item : items )
		{
			if( item.isSelected() )
			{
				addValueToTargets(item.getValue(), targets, itemxml);
			}
		}
	}

	@Override
	public void setValues(@Nullable String... values)
	{
		for( Item item : items )
		{
			item.setSelected(false);
			empty = true;
		}

		if( values == null )
		{
			return;
		}

		for( String element : values )
		{
			for( Item item : items )
			{
				if( item.getValue().equals(element) )
				{
					item.setSelected(true);
					empty = false;
				}
			}
		}
	}

	@Override
	public void resetToDefaults()
	{
		items.clear();
		empty = true;
		for( WizardControlItem item : controlBean.getItems() )
		{
			String name = evalString(item.getName());
			String value = evalString(item.getValue());
			String defaultVal = evalString(item.getDefault());

			boolean bSel = defaultVal.equals("true");
			empty = empty && !bSel;
			Item oItem = new Item(name, value, bSel);

			items.add(oItem);
		}
	}

	public Item getFirstSelected()
	{
		for( Item item : items )
		{
			if( item.isSelected() )
			{
				return item;
			}
		}
		return null;
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		final Collection<Item> itemsCopy = new HashSet<Item>(items);
		for( Iterator<Item> it = itemsCopy.iterator(); it.hasNext(); )
		{
			if( !it.next().isSelected() )
			{
				it.remove();
			}
		}
		final Collection<String> vals = Sets.newHashSet(Collections2.transform(itemsCopy, new Function<Item, String>()
		{
			@Override
			public String apply(Item item)
			{
				return item.getValue();
			}
		}));

		return getDefaultPowerSearchQuery(vals, false);
	}

	@Override
	public NameValue getNameValue()
	{
		Item fsel = getFirstSelected();
		if( fsel == null )
		{
			return new NameValue("", "");
		}
		return new NameValue(fsel.getName(), fsel.getValue());
	}

	protected boolean selectItems(List<Item> items1, TargetNode node, PropBagEx itemxml)
	{
		boolean anyselected = false;
		Set<String> vals = new HashSet<String>(node.getValues(itemxml));

		for( Item item : items1 )
		{
			if( vals.contains(item.getValue()) )
			{
				item.setSelected(true);
				anyselected = true;
			}
			else
			{
				item.setSelected(false);
			}
		}
		return anyselected;
	}

	@Override
	public boolean isEmpty()
	{
		return empty;
	}

	public void setEmpty(boolean empty)
	{
		this.empty = empty;
	}
}
