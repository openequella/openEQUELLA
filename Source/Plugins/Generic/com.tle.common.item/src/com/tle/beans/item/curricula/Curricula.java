/*
 * Created on Aug 9, 2005
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
