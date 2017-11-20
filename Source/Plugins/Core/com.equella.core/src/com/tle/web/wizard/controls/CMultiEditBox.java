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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.beans.Language;
import com.tle.common.Check;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.controls.WizardPage;

public class CMultiEditBox extends AbstractHTMLControl
{
	private static final long serialVersionUID = 1L;

	private Map<String, String> langValues = new HashMap<String, String>();
	private Collection<String> locales;
	private Map<String, String> localeNames;
	private Locale defaultLocale;

	public CMultiEditBox(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
	}

	public Collection<String> getLocales()
	{
		if( locales == null )
		{
			locales = new ArrayList<String>();
			localeNames = new HashMap<String, String>();
			List<Language> languages = getRepository().getLanguages();
			for( Language language : languages )
			{
				Locale locale = language.getLocale();
				if( defaultLocale == null )
				{
					defaultLocale = locale;
				}
				String locStr = locale.toString();
				locales.add(locStr);
				localeNames.put(locStr, locale.getDisplayName());
			}
		}
		return locales;
	}

	public void setLocales(Collection<String> locales)
	{
		this.locales = locales;
	}

	@Override
	public void saveToDocument(PropBagEx itemxml)
	{
		List<TargetNode> targets = getTargets();
		for( TargetNode node : targets )
		{
			node.clear(itemxml);
			node.addLangNodes(itemxml, langValues);
		}
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		TargetNode firstTarget = getFirstTarget();
		langValues = firstTarget.getLangNode(itemxml, getDefaultLocale());
	}

	public Map<String, String> getLangValues()
	{
		return langValues;
	}

	public void setLangValues(Map<String, String> langValues)
	{
		this.langValues = langValues;
	}

	@Override
	public boolean isEmpty()
	{
		for( String val : langValues.values() )
		{
			if( !Check.isEmpty(val) )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		return null;
	}

	@Override
	public void resetToDefaults()
	{
		// nothing as yet
	}

	@Override
	public void setValues(String... values)
	{
		// nope
	}

	public Locale getDefaultLocale()
	{
		getLocales();
		return defaultLocale;
	}

}
