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

package com.dytech.edge.wizard.beans.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomControl extends WizardControl
{
	private static final long serialVersionUID = 1L;
	private String classType;
	private Map<Object, Object> attributes = new HashMap<Object, Object>();

	public CustomControl()
	{
		// nothing
	}

	public CustomControl(CustomControl cloned)
	{
		cloned.cloneTo(this);
	}

	@Override
	public <T extends WizardControl> void cloneTo(T control)
	{
		super.cloneTo(control);
		CustomControl clonedTo = (CustomControl) control;
		clonedTo.classType = classType;
		clonedTo.attributes = new HashMap<Object, Object>(attributes);
	}

	public Map<Object, Object> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(Map<Object, Object> attributes)
	{
		this.attributes = attributes;
	}

	public boolean getBooleanAttribute(Object key)
	{
		return getBooleanAttribute(key, false);
	}

	public boolean getBooleanAttribute(Object key, boolean defaultValue)
	{
		Object o = getAttributes().get(key);
		if( o instanceof Boolean )
		{
			return (Boolean) o;
		}
		else if( o instanceof String )
		{
			return Boolean.valueOf((String) o);
		}
		else
		{
			return defaultValue;
		}
	}

	public <T extends Enum<T>> T get(Object key, Class<T> enumType, T defaultValue)
	{
		Object o = getAttributes().get(key);
		if( o == null )
		{
			return defaultValue;
		}
		else if( o instanceof String )
		{
			return Enum.valueOf(enumType, (String) o);
		}
		else
		{
			return enumType.cast(o);
		}
	}

	public <T> List<T> ensureListAttribute(Object key)
	{
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) attributes.get(key);
		if( result == null )
		{
			result = new ArrayList<T>();
			attributes.put(key, result);
		}
		return result;
	}

	public <T> Set<T> ensureSetAttribute(Object key)
	{
		@SuppressWarnings("unchecked")
		Set<T> result = (Set<T>) attributes.get(key);
		if( result == null )
		{
			result = new HashSet<T>();
			attributes.put(key, result);
		}
		return result;
	}

	@Override
	public String getClassType()
	{
		return classType;
	}

	public void setClassType(String classType)
	{
		this.classType = classType;
	}

}
