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
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.controls.WizardPage;

public class CCustomControl extends AbstractHTMLControl
{
	private static final long serialVersionUID = 1L;
	protected List<String> values = new ArrayList<String>();

	public CCustomControl(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
	}

	@Override
	public void setValues(String... vals)
	{
		values.clear();
		if( vals != null )
		{
			values.addAll(Arrays.asList(vals));
		}
	}

	@Override
	public boolean isEmpty()
	{
		return values.isEmpty();
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		if( hasTargets() )
		{
			TargetNode firstTarget = getFirstTarget();
			if( !firstTarget.nodeExists(itemxml, 0) )
			{
				doEvaluate();
			}
			else
			{
				values.clear();
				values.addAll(firstTarget.getValues(itemxml));
			}
		}
	}

	@Override
	public void saveToDocument(PropBagEx itemxml) throws Exception
	{
		if( hasTargets() )
		{
			clearTargets(itemxml);
			for( String value : values )
			{
				if( value != null )
				{
					addValueToTargets(value, targets, itemxml);
				}
			}
		}
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		if( hasTargets() )
		{
			return getDefaultPowerSearchQuery(values, true);
		}
		return null;
	}

	public List<String> getValues()
	{
		return values;
	}

	public String getValueWithIndex(int index)
	{
		if( index >= values.size() )
		{
			return null;
		}
		else
		{
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
		else
		{
			return ""; //$NON-NLS-1$
		}
	}

	public void setValue(String value)
	{
		if( values.size() == 0 )
		{
			values.add(value);
		}
		else
		{
			values.set(0, value);
		}
	}

	@Override
	public void resetToDefaults()
	{
		// no thing
	}

	public boolean hasTargets()
	{
		return (targets != null && targets.size() > 0);
	}
}
