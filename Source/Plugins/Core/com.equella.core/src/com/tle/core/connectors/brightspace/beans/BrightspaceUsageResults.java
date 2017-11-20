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

package com.tle.core.connectors.brightspace.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Aaron
 *
 */
@XmlRootElement
public class BrightspaceUsageResults
{
	@JsonProperty("TotalEquellaLinkUsageCount")
	private int totalEquellaLinkUsageCount;
	@JsonProperty("ResultSetCount")
	private int resultSetCount;
	@JsonProperty("Next")
	private String next;
	@JsonProperty("Objects")
	private BrightspaceEquellaLink[] objects;

	public int getTotalEquellaLinkUsageCount()
	{
		return totalEquellaLinkUsageCount;
	}

	public void setTotalEquellaLinkUsageCount(int totalEquellaLinkUsageCount)
	{
		this.totalEquellaLinkUsageCount = totalEquellaLinkUsageCount;
	}

	public int getResultSetCount()
	{
		return resultSetCount;
	}

	public void setResultSetCount(int resultSetCount)
	{
		this.resultSetCount = resultSetCount;
	}

	public String getNext()
	{
		return next;
	}

	public void setNext(String next)
	{
		this.next = next;
	}

	public BrightspaceEquellaLink[] getObjects()
	{
		return objects;
	}

	public void setObjects(BrightspaceEquellaLink[] objects)
	{
		this.objects = objects;
	}
}
