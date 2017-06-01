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

package com.tle.beans.taxonomy;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

@XmlRootElement
public class TermBean extends AbstractExtendableBean
{
	private String term;
	private String fullTerm;
	private String parentUuid;
	private String uuid;
	private int index = 0;
	private Map<String, String> data;
	private boolean readonly;

	public String getTerm()
	{
		return term;
	}

	public void setTerm(String term)
	{
		this.term = term;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public Map<String, String> getData()
	{
		return data;
	}

	public void setData(Map<String, String> data)
	{
		this.data = data;
	}

	public String getFullTerm()
	{
		return fullTerm;
	}

	public void setFullTerm(String fullTerm)
	{
		this.fullTerm = fullTerm;
	}

	public boolean isReadonly()
	{
		return readonly;
	}

	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

	public String getParentUuid()
	{
		return parentUuid;
	}

	public void setParentUuid(String parentUuid)
	{
		this.parentUuid = parentUuid;
	}
}
