/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.standard.model;

import java.util.ArrayList;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public abstract class MutableListModel<T> implements HtmlMutableListModel<T>
{
	@Override
	public List<Option<T>> getOptions(SectionInfo info)
	{
		List<Option<T>> opts = new ArrayList<Option<T>>();
		for( String value : getListState(info).values )
		{
			T obj = convertStringToObject(info, value);
			opts.add(convertToOption(info, obj, value));
		}
		return opts;
	}

	@Override
	public List<String> getValues(SectionInfo info)
	{
		return getListState(info).values;
	}

	public void add(SectionInfo info, String value)
	{
		ListState<T> listState = getListState(info);
		listState.values.add(value);
	}

	public void remove(SectionInfo info, String value)
	{
		ListState<T> listState = getListState(info);
		listState.values.remove(value);
	}

	public boolean contains(SectionInfo info, String value)
	{
		ListState<T> listState = getListState(info);
		return listState.values.contains(value);
	}

	public void clear(SectionInfo info)
	{
		ListState<T> listState = getListState(info);
		listState.values.clear();
	}

	public boolean isEmpty(SectionInfo info)
	{
		ListState<T> listState = getListState(info);
		return listState.values.isEmpty();
	}

	protected Option<T> convertToOption(SectionInfo info, T obj, String value)
	{
		return SimpleHtmlListModel.defaultConvertToOption(obj);
	}

	protected ListState<T> getListState(SectionInfo info)
	{
		ListState<T> listState = info.getAttribute(this);
		if( listState == null )
		{
			listState = new ListState<T>();
			info.setAttribute(this, listState);
		}
		return listState;
	}

	@Override
	public void setValues(SectionInfo info, List<String> strings)
	{
		ListState<T> listState = getListState(info);
		listState.values.clear();
		listState.values.addAll(strings);
	}

	protected abstract T convertStringToObject(SectionInfo info, String str);

	protected static class ListState<T>
	{
		List<String> values = new ArrayList<String>();
	}
}
