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

package com.tle.web.itemlist.item;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tle.web.itemlist.ListEntry;

public class ListSettings<T extends ListEntry>
{
	private boolean editable = true;
	private Collection<String> hilightedWords;
	private List<T> entries;
	private Map<Object, Object> attrs = Maps.newHashMap();

	public Collection<String> getHilightedWords()
	{
		return hilightedWords;
	}

	public void setHilightedWords(Collection<String> hilightedWords)
	{
		this.hilightedWords = hilightedWords;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public List<T> getEntries()
	{
		return entries;
	}

	public void setEntries(List<T> entries)
	{
		this.entries = entries;
	}

	@SuppressWarnings("unchecked")
	public <A> A getAttribute(Object key)
	{
		return (A) attrs.get(key);
	}

	public void setAttribute(Object key, Object value)
	{
		attrs.put(key, value);
	}

}
