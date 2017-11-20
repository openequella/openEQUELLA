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

package com.tle.common.interfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CsvList
{
	private final List<String> values;

	public CsvList(List<String> values)
	{
		this.values = values;
	}

	public List<String> getValues()
	{
		return values;
	}

	@SuppressWarnings("nls")
	public static CsvList valueOf(String value)
	{
		final List<String> values = new ArrayList<String>();
		final String[] valsArr = value.split(",");
		for( String val : valsArr )
		{
			val = val.trim();
			if( !val.isEmpty() )
			{
				values.add(val);
			}
		}
		return new CsvList(values);
	}

	public static List<String> asList(CsvList list, String... defaultValues)
	{
		if( list == null )
		{
			if( defaultValues.length == 0 )
			{
				return Collections.emptyList();
			}
			List<String> all = new ArrayList<String>();
			for(String val : defaultValues)
			{
				all.add(val);
			}
			return all;
		}
		return list.getValues();
	}
}
