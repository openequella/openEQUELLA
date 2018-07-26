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

package com.tle.core.reporting.birttypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.ValueThoroughIterator;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.google.common.base.Throwables;
import com.tle.common.i18n.LangUtils;

public abstract class AbstractListBirtType extends AbstractBirtType
{
	private Map<String, String> displayTexts;

	public AbstractListBirtType(IScalarParameterDefn def, int paramNum)
	{
		this(def, paramNum, null);
	}

	public AbstractListBirtType(IScalarParameterDefn def, int paramNum, IParameterGroupDefn group)
	{
		super(def, paramNum, group);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setupControl(IGetParameterDefinitionTask paramTask, WizardControl control)
	{
		super.setupControl(paramTask, control);
		Object defaultValue = paramTask.getDefaultValue(scalarDef);
		Collection<IParameterSelectionChoice> selectionList = paramTask.getSelectionList(scalarDef.getName());
		List<?> errors = paramTask.getErrors();
		for( Object o : errors )
		{
			if( o instanceof Throwable )
			{
				throw Throwables.propagate((Throwable) o);
			}
			throw new RuntimeException("An unknown error occurred");
		}

		List<WizardControlItem> items = control.getItems();
		displayTexts = new HashMap<String, String>();
		for( IParameterSelectionChoice choice : selectionList )
		{
			String valString = String.valueOf(choice.getValue());
			String label = choice.getLabel() != null ? choice.getLabel() : String.valueOf(choice.getValue());
			WizardControlItem item = new WizardControlItem(LangUtils.createTextTempLangugageBundle(label), valString);
			if( choice.getValue().equals(defaultValue) )
			{
				item.setDefaultOption(true);
			}
			items.add(item);

			displayTexts.put(choice.getValue().toString(), choice.getLabel());
		}
	}

	@Override
	public void convertToParams(PropBagEx docXml, Map<String, String[]> params)
	{
		List<String> paramVals = new ArrayList<String>();
		ValueThoroughIterator vals = docXml.iterateAllValues(targetNode);
		while( vals.hasNext() )
		{
			paramVals.add(vals.next());
		}
		if( !paramVals.isEmpty() )
		{
			params.put(scalarDef.getName(), paramVals.toArray(new String[paramVals.size()]));
		}
		else
		{
			params.remove(scalarDef.getName());
		}
	}

	@Override
	public void convertToXml(PropBagEx docXml, Map<String, String[]> params)
	{
		String[] vals = params.get(scalarDef.getName());
		if( vals != null )
		{
			for( String val : vals )
			{
				docXml.createNode(targetNode, val);
			}
		}
	}

	@Override
	public String[] getDisplayTexts(PropBagEx docXml)
	{
		List<String> texts = new ArrayList<String>();
		ValueThoroughIterator vals = docXml.iterateAllValues(targetNode);
		while( vals.hasNext() )
		{
			String textVal = displayTexts.get(vals.next());
			if (textVal != null) {
				texts.add(textVal);
			}
		}
		return texts.toArray(new String[texts.size()]);
	}
}
