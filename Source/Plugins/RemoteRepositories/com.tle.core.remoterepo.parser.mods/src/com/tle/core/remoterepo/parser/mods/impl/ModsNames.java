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

package com.tle.core.remoterepo.parser.mods.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.core.xml.XmlDocument;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class ModsNames extends ModsPart
{
	// protected Map<String, ModsName> names;
	protected Collection<ModsName> names;

	public ModsNames(XmlDocument xml, Node context)
	{
		super(xml, context);
	}

	public Collection<String> getNames()
	{
		if( names == null )
		{
			names = new HashSet<ModsName>(); // new HashMap<String, ModsName>();
			for( Node name : xml.nodeList("name[@type='personal']", context) )
			{
				names.add(new ModsName(name));
			}
		}
		if( names.isEmpty() )
		{
			return null;
		}
		final List<String> stringNames = Lists
			.newArrayList(Collections2.transform(names, new Function<ModsName, String>()
			{
				@Override
				public String apply(ModsName modsName)
				{
					// TODO: should be part of a loose MODS name
					String name = modsName.getDisplayForm();
					if( Check.isEmpty(name) )
					{
						name = modsName.getNamePart();

						if( Check.isEmpty(name) )
						{
							name = modsName.getText();
							if( Check.isEmpty(name) )
							{
								return null;
							}
						}
					}
					return name;
				}
			}));
		if( allEmpty(stringNames) )
		{
			return null;
		}
		return stringNames;
	}

	private boolean allEmpty(Collection<String> strings)
	{
		if( strings == null )
		{
			return true;
		}
		for( String string : strings )
		{
			if( !Check.isEmpty(string) )
			{
				return false;
			}
		}
		return true;
	}

	protected class ModsName
	{
		protected Node nameContext;

		protected ModsName(Node nameContext)
		{
			this.nameContext = nameContext;
		}

		String getText()
		{
			return xml.nodeValue("/", nameContext);
		}

		String getNamePart()
		{
			return xml.nodeValue("namePart", nameContext);
		}

		String getDisplayForm()
		{
			return xml.nodeValue("displayForm", nameContext);
		}

		String getAffiliation()
		{
			return xml.nodeValue("affiliation", nameContext);
		}

		String getRole()
		{
			throw new UnsupportedOperationException();
		}

		String getDescription()
		{
			return xml.nodeValue("description", nameContext);
		}
	}
}
