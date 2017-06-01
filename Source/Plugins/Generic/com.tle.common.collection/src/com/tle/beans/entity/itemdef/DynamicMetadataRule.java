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

package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.Objects;

import com.tle.common.Check;
import com.tle.common.security.TargetList;

/**
 * @author Andrew Gibb
 */
public class DynamicMetadataRule implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private String path;
	private String type;
	private TargetList targetList;

	public DynamicMetadataRule()
	{
		super();
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public TargetList getTargetList()
	{
		if( targetList == null )
		{
			targetList = new TargetList();
		}
		return targetList;
	}

	public void setTargetList(TargetList targetList)
	{
		this.targetList = targetList;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof DynamicMetadataRule) )
		{
			return false;
		}

		return Objects.equals(id, ((DynamicMetadataRule) obj).id);
	}

	@Override
	public int hashCode()
	{
		return Check.getHashCode(id, name, path, type);
	}
}
