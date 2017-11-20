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

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.Button;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.common.Check;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.controls.WizardPage;

/**
 */
public class CButton extends AbstractHTMLControl
{
	private static final long serialVersionUID = 1L;
	private boolean pressed;

	public CButton(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
		Button thebut = (Button) controlBean;
		String action = thebut.getAction();
		if( !Check.isEmpty(action) )
		{
			int afterind = action.indexOf("//aftersave"); //$NON-NLS-1$
			if( afterind >= 0 )
			{
				thebut.setAfterSaveScript(action.substring(afterind + 11));
			}
		}
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		pressed = false;
	}

	@Override
	public void saveToDocument(PropBagEx itemxml)
	{
		// DO NOTHING
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		return null;
	}

	@Override
	public void resetToDefaults()
	{
		// DO NOTHING
	}

	@Override
	public void setValues(String... values)
	{
		// DO NOTHING
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public void afterSaveValidate()
	{
		super.afterSaveValidate();
		if( pressed )
		{
			execScript(((Button) controlBean).getAction());
		}
	}

	public void setActionFired(boolean b)
	{
		pressed = b;
	}
}
