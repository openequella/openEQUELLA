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

package com.dytech.edge.ejb.helpers.metadata.mapping;

import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.mapping.IMSMapping.MappingType;

/**
 * @author aholland
 */
public class GenericPackageMapping extends Mapping
{
	private final PropBagEx xml;

	public GenericPackageMapping(String path, PropBagEx xml)
	{
		super(path, "", MappingType.COMPOUND, false); //$NON-NLS-1$
		this.xml = xml;
	}

	@Override
	public void update(PropBagEx item)
	{
		PropBagEx sub = item.newSubtree(getPath());
		sub.appendChildren("", xml); //$NON-NLS-1$

		// We need to do this to get attributes from the root node
		// (Just in case - but should be rare)
		for( Map.Entry<String, String> entry : xml.getAttributesForNode("").entrySet() )
		{
			sub.setNode("/@" + entry.getKey(), entry.getValue());
		}
	}
}
