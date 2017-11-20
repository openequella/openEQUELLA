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
import java.util.Arrays;
import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.NameValue;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.controls.WizardPage;

@NonNullByDefault
public abstract class EditableCtrl extends AbstractHTMLControl
{
	private static final long serialVersionUID = 1L;

	private final List<String> values = new ArrayList<String>();
	private final Object valuesLock = new Object();

	public EditableCtrl(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
	}

	protected void clearValues()
	{
		synchronized( valuesLock )
		{
			values.clear();
		}
	}

	@Override
	public void setValues(@Nullable String... vals)
	{
		synchronized( valuesLock )
		{
			values.clear();
			if( vals != null )
			{
				values.addAll(Arrays.asList(vals));
			}
		}
	}

	protected void addValue(String val)
	{
		synchronized( valuesLock )
		{
			values.add(val);
		}
	}

	@Override
	public boolean isEmpty()
	{
		synchronized( valuesLock )
		{
			return values.isEmpty();
		}
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		TargetNode firstTarget = getFirstTarget();
		if( !firstTarget.nodeExists(itemxml, 0) )
		{
			doEvaluate();
		}
		else
		{
			synchronized( valuesLock )
			{
				values.clear();
				values.addAll(firstTarget.getValues(itemxml));
			}
		}
	}

	@Override
	public void saveToDocument(PropBagEx itemxml) throws Exception
	{
		clearTargets(itemxml);
		for( String value : getValues() )
		{
			addValueToTargets(value, targets, itemxml);
		}
	}

	@Nullable
	@Override
	public BaseQuery getPowerSearchQuery()
	{
		return getDefaultPowerSearchQuery(getValues(), true);
	}

	public ImmutableCollection<String> getValues()
	{
		synchronized( valuesLock )
		{
			return ImmutableList.copyOf(values);
		}
	}

	@Nullable
	public String getValueWithIndex(int index)
	{
		synchronized( valuesLock )
		{
			if( index >= values.size() )
			{
				return null;
			}
			return values.get(index);
		}
	}

	public String getValue()
	{
		String value = getValueWithIndex(0);
		if( value != null )
		{
			return value;
		}
		return Constants.BLANK;
	}

	@Override
	public NameValue getNameValue()
	{
		String val = getValue();
		return new NameValue(val, val);
	}
}
