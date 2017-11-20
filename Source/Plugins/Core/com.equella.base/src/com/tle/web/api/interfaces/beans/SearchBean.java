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

package com.tle.web.api.interfaces.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Aaron
 */
@XmlRootElement
public class SearchBean<T> extends AbstractExtendableBean
{
	private int start;
	private int length;
	private int available;
	private List<T> results;

	public int getStart()
	{
		return start;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public int getLength()
	{
		return length;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public int getAvailable()
	{
		return available;
	}

	public void setAvailable(int available)
	{
		this.available = available;
	}

	public List<T> getResults()
	{
		return results;
	}

	public void setResults(List<T> results)
	{
		this.results = results;
	}
}
