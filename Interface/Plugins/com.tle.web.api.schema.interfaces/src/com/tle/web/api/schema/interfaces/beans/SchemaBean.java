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

package com.tle.web.api.schema.interfaces.beans;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

@XmlRootElement
public class SchemaBean extends BaseEntityBean
{
	private String namePath;
	private String descriptionPath;
	private Map<String, SchemaNodeBean> definition;

	public String getNamePath()
	{
		return namePath;
	}

	public void setNamePath(String namePath)
	{
		this.namePath = namePath;
	}

	public String getDescriptionPath()
	{
		return descriptionPath;
	}

	public void setDescriptionPath(String descriptionPath)
	{
		this.descriptionPath = descriptionPath;
	}

	public Map<String, SchemaNodeBean> getDefinition()
	{
		return definition;
	}

	public void setDefinition(Map<String, SchemaNodeBean> definition)
	{
		this.definition = definition;
	}

}
