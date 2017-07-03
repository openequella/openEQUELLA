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

package com.tle.core.util.ims.beans;

import com.dytech.devlib.PropBagEx;
import com.tle.core.xstream.XMLData;
import com.tle.core.xstream.XMLDataMappings;
import com.tle.core.xstream.mapping.PropBagMapping;

/**
 * @author Aaron
 */
public class IMSCustomData implements XMLData
{
	private static final long serialVersionUID = 1L;

	private static XMLDataMappings mappings;

	private PropBagEx xml;

	public PropBagEx getXml()
	{
		return xml;
	}

	public void setXml(PropBagEx xml)
	{
		this.xml = xml;
	}

	@Override
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings();
			mappings.addNodeMapping(new PropBagMapping("xml", "custom", true));
		}
		return mappings;
	}
}
