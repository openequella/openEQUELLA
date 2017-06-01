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

package com.tle.beans.item.curricula;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.dytech.common.xml.XMLData;
import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.DataMapping;
import com.dytech.common.xml.mapping.SetMapping;

public class Curricula implements XMLData
{
	private static final long serialVersionUID = 1L;

	private Set<Curriculum> curriculum = new LinkedHashSet<Curriculum>();

	private static XMLDataMappings mappings;

	public Curricula()
	{
		super();
	}

	@Override
	@SuppressWarnings("nls")
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings();
			// The intent presumably is to return the implementation class, so
			// we ignore the 'loose coupling' Sonar warning.
			mappings.addNodeMapping(new SetMapping("curriculum", "curriculum", HashSet.class, new DataMapping("", "", // NOSONAR
				Curriculum.class)));
		}
		return mappings;
	}

	public Set<Curriculum> getCurriculum()
	{
		return curriculum;
	}

	public void setCurriculum(Set<Curriculum> curriculum)
	{
		this.curriculum = curriculum;
	}
}
