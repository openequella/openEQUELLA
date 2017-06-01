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

package com.tle.common.searching;

import java.util.Date;

/**
 * @author aholland
 */
public class DateFilter
{
	public enum Format
	{
		ISO, LONG
	}

	private final Format format;
	private final String indexFieldName;
	private final Date[] value;

	public DateFilter(String indexFieldName, Date[] value)
	{
		this(indexFieldName, value, Format.ISO);
	}

	public DateFilter(String indexFieldName, Date[] value, Format format)
	{
		this.indexFieldName = indexFieldName;
		this.value = value;
		this.format = format;
	}

	public String getIndexFieldName()
	{
		return indexFieldName;
	}

	public Date[] getRange()
	{
		return value;
	}

	public Format getFormat()
	{
		return format;
	}
}
