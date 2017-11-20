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

package com.tle.core.fedsearch.impl;

import java.util.Collection;

import com.dytech.devlib.PropBagEx;
import com.tle.core.fedsearch.GenericRecord;

/**
 * @author aholland
 */
public class NullRecord implements GenericRecord
{
	@Override
	public Collection<String> getAuthors()
	{
		return null;
	}

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public String getIsbn()
	{
		return null;
	}

	@Override
	public String getIssn()
	{
		return null;
	}

	@Override
	public String getLccn()
	{
		return null;
	}

	@Override
	public String getPhysicalDescription()
	{
		return null;
	}

	@Override
	public String getTitle()
	{
		return null;
	}

	@Override
	public String getType()
	{
		return null;
	}

	@Override
	public String getUri()
	{
		return null;
	}

	@Override
	public String getUrl()
	{
		return null;
	}

	@Override
	public PropBagEx getXml()
	{
		return new PropBagEx();
	}
}
