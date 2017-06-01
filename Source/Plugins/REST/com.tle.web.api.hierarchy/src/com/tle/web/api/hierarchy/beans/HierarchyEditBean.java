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

package com.tle.web.api.hierarchy.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.item.equella.interfaces.beans.ItemDefinitionScriptBean;
import com.tle.web.api.item.equella.interfaces.beans.SchemaScriptBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;

/**
 * @author larry
 */
@XmlRootElement
public class HierarchyEditBean extends AbstractExtendableBean
{
	private String uuid;
	private String name;
	private String shortDescription;
	private String longDescription;
	private String subTopicsSectionName;
	private String resultsSectionName;
	//FIXME: need lang strings versions for all above names
	private String powerSearchUuid;
	private String freetext;
	private HierarchyEditBean parent;
	private Boolean showResults;
	private Boolean inheritFreetext;
	private Boolean hideSubtopicsWithNoResults;
	private String virtualisationPath;
	private String virtualisationId;
	//private List<AttributeBean> attributes;
	private List<HierarchyEditBean> subTopics;
	private List<ItemBean> keyResources;
	private List<SchemaScriptBean> inheritedSchemaScripts;
	private List<SchemaScriptBean> additionalSchemaScripts;
	private List<SchemaScriptBean> eligibleSchemaScripts;
	private List<ItemDefinitionScriptBean> inheritedCollectionScripts;
	private List<ItemDefinitionScriptBean> additionalCollectionScripts;
	private List<ItemDefinitionScriptBean> eligibleCollectionScripts;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String id)
	{
		this.uuid = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getShortDescription()
	{
		return shortDescription;
	}

	public void setShortDescription(String shortDescription)
	{
		this.shortDescription = shortDescription;
	}

	public String getLongDescription()
	{
		return longDescription;
	}

	public void setLongDescription(String longDescription)
	{
		this.longDescription = longDescription;
	}

	public String getSubTopicsSectionName()
	{
		return subTopicsSectionName;
	}

	public void setSubTopicsSectionName(String subTopicsSectionName)
	{
		this.subTopicsSectionName = subTopicsSectionName;
	}

	public String getResultsSectionName()
	{
		return resultsSectionName;
	}

	public void setResultsSectionName(String resultsSectionName)
	{
		this.resultsSectionName = resultsSectionName;
	}

	public String getPowerSearchUuid()
	{
		return powerSearchUuid;
	}

	public void setPowerSearchUuid(String powerSearchUuid)
	{
		this.powerSearchUuid = powerSearchUuid;
	}

	public String getFreetext()
	{
		return freetext;
	}

	public void setFreetext(String freetext)
	{
		this.freetext = freetext;
	}

	public HierarchyEditBean getParent()
	{
		return parent;
	}

	public void setParent(HierarchyEditBean parent)
	{
		this.parent = parent;
	}

	public Boolean isShowResults()
	{
		return showResults;
	}

	public void setShowResults(Boolean showResults)
	{
		this.showResults = showResults;
	}

	public Boolean isInheritFreetext()
	{
		return inheritFreetext;
	}

	public void setInheritFreetext(Boolean inheritFreetext)
	{
		this.inheritFreetext = inheritFreetext;
	}

	public Boolean isHideSubtopicsWithNoResults()
	{
		return hideSubtopicsWithNoResults;
	}

	public void setHideSubtopicsWithNoResults(Boolean hideSubtopicsWithNoResults)
	{
		this.hideSubtopicsWithNoResults = hideSubtopicsWithNoResults;
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

	//	public List<AttributeBean> getAttributes()
	//	{
	//		return attributes;
	//	}
	//
	//	public void setAttributes(List<AttributeBean> attributes)
	//	{
	//		this.attributes = attributes;
	//	}

	public List<ItemBean> getKeyResources()
	{
		return keyResources;
	}

	public List<HierarchyEditBean> getSubTopics()
	{
		return subTopics;
	}

	public void setSubTopics(List<HierarchyEditBean> subTopics)
	{
		this.subTopics = subTopics;
	}

	public void setKeyResources(List<ItemBean> keyResources)
	{
		this.keyResources = keyResources;
	}

	public List<SchemaScriptBean> getAdditionalSchemaScripts()
	{
		return additionalSchemaScripts;
	}

	public void setAdditionalSchemaScripts(List<SchemaScriptBean> additionalSchemaScripts)
	{
		this.additionalSchemaScripts = additionalSchemaScripts;
	}

	public List<SchemaScriptBean> getEligibleSchemaScripts()
	{
		return eligibleSchemaScripts;
	}

	public void setEligibleSchemaScripts(List<SchemaScriptBean> eligibleSchemaScripts)
	{
		this.eligibleSchemaScripts = eligibleSchemaScripts;
	}

	public List<SchemaScriptBean> getInheritedSchemaScripts()
	{
		return inheritedSchemaScripts;
	}

	public void setInheritedSchemaScripts(List<SchemaScriptBean> inheritedSchemaScripts)
	{
		this.inheritedSchemaScripts = inheritedSchemaScripts;
	}

	public List<ItemDefinitionScriptBean> getAdditionalCollectionScripts()
	{
		return additionalCollectionScripts;
	}

	public void setAdditionalCollectionScripts(List<ItemDefinitionScriptBean> additionalCollectionScripts)
	{
		this.additionalCollectionScripts = additionalCollectionScripts;
	}

	public List<ItemDefinitionScriptBean> getEligibleCollectionScripts()
	{
		return eligibleCollectionScripts;
	}

	public void setEligibleCollectionScripts(List<ItemDefinitionScriptBean> eligibleCollectionScripts)
	{
		this.eligibleCollectionScripts = eligibleCollectionScripts;
	}

	public List<ItemDefinitionScriptBean> getInheritedCollectionScripts()
	{
		return inheritedCollectionScripts;
	}

	public void setInheritedCollectionScripts(List<ItemDefinitionScriptBean> inheritedCollectionScripts)
	{
		this.inheritedCollectionScripts = inheritedCollectionScripts;
	}

	/**
	 * Simplified version of NameValue class - a minimal interface avoids
	 * superfluous duplicated properties
	 */
	//	public static class AttributeBean
	//	{
	//		private String key;
	//
	//		private String value;
	//
	//		public String getKey()
	//		{
	//			return key;
	//		}
	//
	//		public void setKey(String key)
	//		{
	//			this.key = key;
	//		}
	//
	//		public String getValue()
	//		{
	//			return value;
	//		}
	//
	//		public void setValue(String value)
	//		{
	//			this.value = value;
	//		}
	//	}
}
