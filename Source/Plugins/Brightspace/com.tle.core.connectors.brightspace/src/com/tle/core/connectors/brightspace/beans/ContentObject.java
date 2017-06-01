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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates both the Module and Topic types.  The type prop will determine which type it is.
 * 
 * @author Aaron
 *
 */
@XmlRootElement
public class ContentObject
{
	public static final int TYPE_MODULE = 0;
	public static final int TYPE_TOPIC = 1;

	//Common
	@JsonProperty("Id")
	private Long id;
	@JsonProperty("Title")
	private String title;
	@JsonProperty("ShortTitle")
	private String shortTitle;
	@JsonProperty("Type")
	private int type; //module == 0, topic == 1
	@JsonProperty("Description")
	private RichText description;
	@JsonProperty("ParentModuleId")
	private Long parentModuleId;
	@JsonProperty("IsHidden")
	private Boolean hidden;
	@JsonProperty("IsLocked")
	private Boolean locked;

	//Module
	@JsonProperty("Structure")
	private ContentObject[] structure;
	@JsonProperty("ModuleStartDate")
	private String moduleStartDate;
	@JsonProperty("ModuleEndDate")
	private String moduleEndDate;
	@JsonProperty("ModuleDueDate")
	private String moduleDueDate;

	//Topic
	@JsonProperty("TopicType")
	private Integer topicType;
	@JsonProperty("Url")
	private String url;
	//Possibly always needed...
	@JsonInclude(Include.ALWAYS)
	@JsonProperty("StartDate")
	private String startDate;
	//Possibly always needed...
	@JsonInclude(Include.ALWAYS)
	@JsonProperty("EndDate")
	private String endDate;
	//Possibly always needed...
	@JsonInclude(Include.ALWAYS)
	@JsonProperty("DueDate")
	private String dueDate;
	@JsonProperty("OpenAsExternalResource")
	private Boolean openAsExternalResource;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getShortTitle()
	{
		return shortTitle;
	}

	public void setShortTitle(String shortTitle)
	{
		this.shortTitle = shortTitle;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public RichText getDescription()
	{
		return description;
	}

	public void setDescription(RichText description)
	{
		this.description = description;
	}

	public Long getParentModuleId()
	{
		return parentModuleId;
	}

	public void setParentModuleId(Long parentModuleId)
	{
		this.parentModuleId = parentModuleId;
	}

	public Boolean getHidden()
	{
		return hidden;
	}

	public void setHidden(Boolean hidden)
	{
		this.hidden = hidden;
	}

	public Boolean getLocked()
	{
		return locked;
	}

	public void setLocked(Boolean locked)
	{
		this.locked = locked;
	}

	public ContentObject[] getStructure()
	{
		return structure;
	}

	public void setStructure(ContentObject[] structure)
	{
		this.structure = structure;
	}

	public String getModuleStartDate()
	{
		return moduleStartDate;
	}

	public void setModuleStartDate(String moduleStartDate)
	{
		this.moduleStartDate = moduleStartDate;
	}

	public String getModuleEndDate()
	{
		return moduleEndDate;
	}

	public void setModuleEndDate(String moduleEndDate)
	{
		this.moduleEndDate = moduleEndDate;
	}

	public String getModuleDueDate()
	{
		return moduleDueDate;
	}

	public void setModuleDueDate(String moduleDueDate)
	{
		this.moduleDueDate = moduleDueDate;
	}

	public Integer getTopicType()
	{
		return topicType;
	}

	public void setTopicType(Integer topicType)
	{
		this.topicType = topicType;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getStartDate()
	{
		return startDate;
	}

	public void setStartDate(String startDate)
	{
		this.startDate = startDate;
	}

	public String getEndDate()
	{
		return endDate;
	}

	public void setEndDate(String endDate)
	{
		this.endDate = endDate;
	}

	public String getDueDate()
	{
		return dueDate;
	}

	public void setDueDate(String dueDate)
	{
		this.dueDate = dueDate;
	}

	public Boolean getOpenAsExternalResource()
	{
		return openAsExternalResource;
	}

	public void setOpenAsExternalResource(Boolean openAsExternalResource)
	{
		this.openAsExternalResource = openAsExternalResource;
	}

}
