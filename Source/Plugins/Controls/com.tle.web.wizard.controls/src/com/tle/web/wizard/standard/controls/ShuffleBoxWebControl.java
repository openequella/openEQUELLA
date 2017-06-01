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
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.Item;

/**
 * @author jmaginnis
 */
@Bind
public class ShuffleBoxWebControl extends AbstractOptionControl
{
	@Component(stateful = false)
	private MultiSelectionList<Item> list;

	@Override
	public MultiSelectionList<Item> getList()
	{
		return list;
	}

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		super.setWrappedControl(control);
		if( control.getSize1() == 0 )
		{
			control.setSize1(12);
		}

		if( control.getSize2() == 0 )
		{
			control.setSize2(240);
		}
	}

	@Override
	protected String getTemplate()
	{
		setGroupLabellNeeded(true);
		return "shufflebox.ftl"; //$NON-NLS-1$
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return list;
	}
}
