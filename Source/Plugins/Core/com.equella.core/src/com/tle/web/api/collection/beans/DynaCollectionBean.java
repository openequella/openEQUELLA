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

package com.tle.web.api.collection.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;
import com.tle.web.api.item.equella.interfaces.beans.ItemDefinitionScriptBean;
import com.tle.web.api.item.equella.interfaces.beans.SchemaScriptBean;

/**
 * @author larry
 */
@XmlRootElement
public class DynaCollectionBean extends BaseEntityBean
{
	private String freetext;
	private List<String> usages;
	private List<ItemDefinitionScriptBean> collectionScripts;
	private List<SchemaScriptBean> schemaScripts;
	private String virtualisationValue;
	private String virtualisationPath;
	private String virtualisationId;
	private String compoundId;

	public String getFreetext()
	{
		return freetext;
	}

	public void setFreetext(String freetext)
	{
		this.freetext = freetext;
	}

	public List<String> getUsages()
	{
		return usages;
	}

	public void setUsages(List<String> usages)
	{
		this.usages = usages;
	}

	public List<ItemDefinitionScriptBean> getCollectionScripts()
	{
		return collectionScripts;
	}

	public void setCollectionScripts(List<ItemDefinitionScriptBean> collectionScripts)
	{
		this.collectionScripts = collectionScripts;
	}

	public List<SchemaScriptBean> getSchemaScripts()
	{
		return schemaScripts;
	}

	public void setSchemaScripts(List<SchemaScriptBean> schemaScripts)
	{
		this.schemaScripts = schemaScripts;
	}

	public String getVirtualisationValue()
	{
		return virtualisationValue;
	}

	public void setVirtualisationValue(String virtualisationValue)
	{
		this.virtualisationValue = virtualisationValue;
	}

	public String getVirtualisationPath()
	{
		return virtualisationPath;
	}

	public void setVirtualisationPath(String virtualisationPath)
	{
		this.virtualisationPath = virtualisationPath;
	}

	public String getVirtualisationId()
	{
		return virtualisationId;
	}

	public void setVirtualisationId(String virtualisationId)
	{
		this.virtualisationId = virtualisationId;
	}

	public String getCompoundId()
	{
		return compoundId;
	}

	public void setCompoundId(String compoundId)
	{
		this.compoundId = compoundId;
	}
}
