/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.standard.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.tle.web.sections.SectionInfo;

public class NothingListModel<T> implements HtmlListModel<T>
{

	@Override
	public Option<T> getOption(SectionInfo info, String value)
	{
		return null;
	}

	@Override
	public List<Option<T>> getOptions(SectionInfo info)
	{
		return Collections.emptyList();
	}

	@Override
	public List<T> getValues(SectionInfo info, Collection<String> values)
	{
		return Collections.emptyList();
	}

	@Override
	public T getValue(SectionInfo info, String value)
	{
		return null;
	}

	@Override
	public String getDefaultValue(SectionInfo info)
	{
		return null;
	}

	@Override
	public Set<String> getMatchingValues(SectionInfo info, Set<String> values)
	{
		return values;
	}

	@Override
	public String getStringValue(SectionInfo info, T value)
	{
		return null;
	}
}
