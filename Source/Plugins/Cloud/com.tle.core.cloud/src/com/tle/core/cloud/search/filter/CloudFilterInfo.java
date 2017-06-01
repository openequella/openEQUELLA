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

package com.tle.core.cloud.search.filter;

import java.util.List;

import com.tle.common.NameValue;

public class CloudFilterInfo
{
	private List<NameValue> languages;
	private List<NameValue> licences;
	private List<NameValue> publishers;
	private List<NameValue> educationLevels;

	public List<NameValue> getLanguages()
	{
		return languages;
	}

	public void setLanguages(List<NameValue> languages)
	{
		this.languages = languages;
	}

	public List<NameValue> getLicences()
	{
		return licences;
	}

	public void setLicences(List<NameValue> licences)
	{
		this.licences = licences;
	}

	public List<NameValue> getPublishers()
	{
		return publishers;
	}

	public void setPublishers(List<NameValue> publishers)
	{
		this.publishers = publishers;
	}

	public List<NameValue> getEducationLevels()
	{
		return educationLevels;
	}

	public void setEducationLevels(List<NameValue> educationLevels)
	{
		this.educationLevels = educationLevels;
	}
}
