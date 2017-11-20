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

package com.tle.core.cloud.search;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.tle.common.searching.SortField;
import com.tle.common.searching.VeryBasicSearch;

public class CloudSearch extends VeryBasicSearch
{
	private SortField[] sortFields;
	private boolean sortReversed;

	private String language;
	private String licence;
	private String educationLevel;
	private String publisher;

	private List<List<String>> formats = Lists.newArrayList();

	@Override
	public SortField[] getSortFields()
	{
		return sortFields;
	}

	public void setSortFields(SortField... sortFields)
	{
		this.sortFields = sortFields;
	}

	public void setSortReversed(boolean sortReversed)
	{
		this.sortReversed = sortReversed;
	}

	@Override
	public boolean isSortReversed()
	{
		return sortReversed;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public String getLicence()
	{
		return licence;
	}

	public void setLicence(String licence)
	{
		this.licence = licence;
	}

	public String getEducationlevel()
	{
		return educationLevel;
	}

	public void setEducationLevel(String educationLevel)
	{
		this.educationLevel = educationLevel;
	}

	public String getPublisher()
	{
		return publisher;
	}

	public void setPublisher(String publisher)
	{
		this.publisher = publisher;
	}

	public List<List<String>> getFormats()
	{
		return formats;
	}

	public void addFormats(List<String> formats)
	{
		this.formats.add(formats);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + Arrays.hashCode(sortFields)
			+ Objects.hash(language, licence, educationLevel, publisher, formats);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof CloudSearch) )
		{
			return false;
		}
		else if( this == obj )
		{
			return true;
		}
		else
		{
			CloudSearch rhs = (CloudSearch) obj;
			return super.equals(obj) && Arrays.equals(sortFields, rhs.sortFields)
				&& Objects.equals(language, rhs.language) && Objects.equals(licence, rhs.licence)
				&& Objects.equals(educationLevel, rhs.educationLevel) && Objects.equals(publisher, rhs.publisher)
				&& Objects.equals(formats, rhs.formats);
		}
	}
}
