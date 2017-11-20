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

package com.tle.core.remoterepo.parser.mods.impl.loose;

import java.util.Collection;
import java.util.Collections;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoterepo.parser.mods.impl.Mods34Record;

/**
 * Results don't usually come back in a strict MODS schema. E.g. title is
 * normally at root level
 * 
 * @author aholland
 */
public class LooseModsRecord extends Mods34Record
{
	protected LoosePhysicalDescription loosePhysicalDescription;

	public LooseModsRecord(PropBagEx xml)
	{
		super(xml);
	}

	@Override
	public String getTitle()
	{
		String current = super.getTitle();
		if( current == null )
		{
			String title = xml.nodeValue("title", context);
			if( Check.isEmpty(title) )
			{
				title = CurrentLocale.get("com.tle.core.remoterepo.parser.mods.notitle");
			}
			return title;
		}
		return current;
	}

	@Override
	public Collection<String> getAuthors()
	{
		Collection<String> current = super.getAuthors();
		if( current == null )
		{
			String single;
			single = xml.nodeValue("name[@role='creator']", context);
			if( Check.isEmpty(single) )
			{
				single = xml.nodeValue("name[@type='personal']", context);
				if( Check.isEmpty(single) )
				{
					single = xml.nodeValue("name", context);
					if( Check.isEmpty(single) )
					{
						single = xml.nodeValue("title[@type='uniform']", context);
						if( Check.isEmpty(single) )
						{
							single = xml.nodeValue("title[@type='alternate']", context);
						}
					}
				}
			}
			if( !Check.isEmpty(single) )
			{
				current = Collections.singleton(single);
			}
		}
		return current;
	}

	@Override
	public String getPhysicalDescription()
	{
		String current = super.getPhysicalDescription();
		if( current == null )
		{
			if( loosePhysicalDescription == null )
			{
				loosePhysicalDescription = new LoosePhysicalDescription(xml, context);
			}
			current = loosePhysicalDescription.getExtent();
		}
		return current;
	}
}
