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

package com.tle.web.search.sort;

import java.util.EventListener;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AbstractSectionEvent;

public class SortOptionsEvent extends AbstractSectionEvent<SortOptionsListener>
{
	private List<Iterable<SortOption>> extraOptions = Lists.newArrayList();
	private AbstractSortOptionsSection section;

	public SortOptionsEvent(AbstractSortOptionsSection section)
	{
		this.section = section;
	}

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return SortOptionsListener.class;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SortOptionsListener listener)
	{
		Iterable<SortOption> options = listener.addSortOptions(info, section);
		if( options != null )
		{
			extraOptions.add(options);
		}
	}

	public List<Iterable<SortOption>> getExtraOptions()
	{
		return extraOptions;
	}

	public void setExtraOptions(List<Iterable<SortOption>> extraOptions)
	{
		this.extraOptions = extraOptions;
	}

}