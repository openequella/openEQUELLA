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

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.mapping.IMSMapping.MappingType;

/**
 * Holds a mapping of an xpath to data.
 */
public class Mapping
{
	private String path;
	private String data;
	private boolean repeat;
	private MappingType type;

	public Mapping(String path, String data)
	{
		this(path, data, MappingType.SIMPLE, false);
	}

	public Mapping(String path, String data, MappingType type, boolean repeat)
	{
		this.path = path;
		this.data = data;
		this.repeat = repeat;
		this.type = type;
	}

	public void update(PropBagEx item)
	{
		String data1 = getData();
		if( data1 != null )
		{
			String path1 = getPath();
			if( isRepeat() )
			{
				item.createNode(path1, data1);
			}
			else
			{
				String oldData = item.getNode(path1);
				if( oldData.length() > 0 )
				{
					oldData += " "; //$NON-NLS-1$
				}
				oldData += data1;

				if( oldData.length() > 0 )
				{
					item.setNode(path1, oldData);
				}
			}
		}
	}

	public MappingType getType()
	{
		return type;
	}

	public String getData()
	{
		return data;
	}

	public void setData(String data)
	{
		this.data = data;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public boolean isRepeat()
	{
		return repeat || type.equals(MappingType.REPEAT);
	}

	public void setRepeat(boolean repeat)
	{
		this.repeat = repeat;
	}

	public boolean isCompound()
	{
		return type.equals(MappingType.COMPOUND);
	}

	@Override
	public boolean equals(Object o)
	{
		if( this == o )
		{
			return true;
		}

		if( !(o instanceof Mapping) )
		{
			return false;
		}

		return data.equals(((Mapping) o).data);
	}

	@Override
	public int hashCode()
	{
		return data.hashCode();
	}
}
