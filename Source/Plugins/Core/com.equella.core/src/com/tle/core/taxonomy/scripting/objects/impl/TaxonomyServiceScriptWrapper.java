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

package com.tle.core.taxonomy.scripting.objects.impl;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.taxonomy.scripting.objects.TaxonomyServiceScriptObject;
import com.tle.core.taxonomy.scripting.types.TaxonomyScriptType;
import com.tle.core.taxonomy.scripting.types.impl.TaxonomyScriptTypeImpl;

/**
 * @author aholland
 */
public class TaxonomyServiceScriptWrapper implements TaxonomyServiceScriptObject
{
	private static final long serialVersionUID = 1L;

	private final TaxonomyService taxonomyService;

	public TaxonomyServiceScriptWrapper(TaxonomyService taxonomyService)
	{
		this.taxonomyService = taxonomyService;
	}

	@Override
	public TaxonomyScriptType getTaxonomyByUuid(String uuid)
	{
		// check to see if the taxo exists first, no point providing a wrapper
		// to a non existent taxonomy
		Taxonomy taxo = taxonomyService.getByUuid(uuid);
		if( taxo != null )
		{
			return new TaxonomyScriptTypeImpl(taxonomyService, uuid);
		}
		return null;
	}

	@Override
	@SuppressWarnings("nls")
	public TaxonomyScriptType getTaxonomyByName(String name)
	{
		TaxonomyScriptType taxScriptObj = null;

		for( Taxonomy tax : taxonomyService.enumerate() )
		{
			if( CurrentLocale.get(tax.getName()).equalsIgnoreCase(name) )
			{
				if( taxScriptObj == null )
				{
					taxScriptObj = getTaxonomyByUuid(tax.getUuid());
				}
				else
				{
					throw new RuntimeException("Multiple taxonomies found for name."
						+ " Identification by UUID is highly recommended and guaranteed" + " to be unique");
				}
			}
		}

		return taxScriptObj;
	}

	@Override
	public void scriptEnter()
	{
		// Nothing to do here
	}

	@Override
	public void scriptExit()
	{
		// Nothing to do here
	}
}
