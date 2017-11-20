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

package com.tle.core.search;

/**
 * A wrapper class for the HierarchyTopic/DynamicCollection, with extra fields
 * to contain a virtualised value string, and a counter. For dynamicCollections,
 * the counter is not initially set (and may not be set at all). For
 * HierarchyTopic the tally is set, and is displayed when navigating into
 * sub-hierarchies
 * 
 * @author larry
 */
public class VirtualisableAndValue<T>
{

	private T vt;

	private String virtualisedValue;

	private int count;

	public VirtualisableAndValue(T ht)
	{
		this.vt = ht;
	}

	public VirtualisableAndValue(T ht, String virtualisedValue, int count)
	{
		this(ht);
		this.virtualisedValue = virtualisedValue;
		this.count = count;
	}

	public T getVt()
	{
		return vt;
	}

	public void setVt(T ht)
	{
		this.vt = ht;
	}

	public String getVirtualisedValue()
	{
		return virtualisedValue;
	}

	public void setVirtualisedValue(String virtualisedValue)
	{
		this.virtualisedValue = virtualisedValue;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}
}
