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

package com.tle.web.wizard.standard.controls;

import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.CCheckBoxGroup;
import com.tle.web.wizard.controls.Item;

/**
 * @author jmaginnis
 */
@Bind
public class CheckBoxGroup extends AbstractOptionControl
{
	@Component(stateful = false, register = false)
	private MultiSelectionList<Item> checkList;
	@Component(stateful = false, register = false)
	private SingleSelectionList<Item> radioList;

	private boolean radio;
	private int columnPercent;

	@Override
	public void registered(String id, SectionTree tree)
	{
		MultiSelectionList<Item> list = getList();
		tree.registerInnerSection(list, id);
		super.registered(id, tree);
		setGroupLabellNeeded(true);
	}

	@SuppressWarnings("nls")
	@Override
	public void setWrappedControl(HTMLControl control)
	{
		if( control.getSize1() == 0 )
		{
			control.setSize1(1);
		}
		radio = ((CCheckBoxGroup) control).getType().equals("radio");
		this.columnPercent = 100 / control.getSize1();
		super.setWrappedControl(control);
	}

	@Override
	protected String getTemplate()
	{
		return "checkboxgroup.ftl"; //$NON-NLS-1$
	}

	@Override
	public MultiSelectionList<Item> getList()
	{
		return radio ? radioList : checkList;
	}

	public boolean isRadio()
	{
		return radio;
	}

	public int getColumnPercent()
	{
		return columnPercent;
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return radio ? radioList : checkList;
	}
}
