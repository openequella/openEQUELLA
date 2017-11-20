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
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.controls.WizardPage;

/**
 * Provides a data model for static HTML controls.
 * 
 * @author Nicholas Read
 */
public class CStaticHTML extends AbstractHTMLControl
{
	private static final long serialVersionUID = 1L;

	protected String html;

	public CStaticHTML(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
		html = CurrentLocale.get(controlBean.getDescription(), "");
	}

	public String getResolvedHtml()
	{
		return evalString(getDescription());
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		// none
	}

	@Override
	public void saveToDocument(PropBagEx itemxml)
	{
		// none
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		return null;
	}

	@Override
	public void resetToDefaults()
	{
		// none
	}

	@Override
	public void setValues(String... values)
	{
		// Nothing to do
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	public String getHtml()
	{
		return html;
	}
}
