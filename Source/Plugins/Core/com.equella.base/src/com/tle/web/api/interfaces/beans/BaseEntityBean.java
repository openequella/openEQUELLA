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

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;

@XmlRootElement
public abstract class BaseEntityBean extends AbstractExtendableBean
{
	private String uuid;

	private I18NString name;
	private I18NStrings nameStrings;
	private I18NString description;
	private I18NStrings descriptionStrings;

	private Date modifiedDate;
	private Date createdDate;
	private UserBean owner;

	private BaseEntitySecurityBean security;

	@JsonProperty(value = "_export")
	private BaseEntityExportBean exportDetails;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public Date getModifiedDate()
	{
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	public Date getCreatedDate()
	{
		return createdDate;
	}

	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	public UserBean getOwner()
	{
		return owner;
	}

	public void setOwner(UserBean owner)
	{
		this.owner = owner;
	}

	public I18NString getName()
	{
		return name;
	}

	public void setName(I18NString name)
	{
		this.name = name;
	}

	public I18NString getDescription()
	{
		return description;
	}

	public void setDescription(I18NString description)
	{
		this.description = description;
	}

	public I18NStrings getNameStrings()
	{
		return nameStrings;
	}

	public void setNameStrings(I18NStrings nameStrings)
	{
		this.nameStrings = nameStrings;
	}

	public I18NStrings getDescriptionStrings()
	{
		return descriptionStrings;
	}

	public void setDescriptionStrings(I18NStrings descriptionStrings)
	{
		this.descriptionStrings = descriptionStrings;
	}

	public void setSecurity(BaseEntitySecurityBean security)
	{
		this.security = security;
	}

	public BaseEntitySecurityBean getSecurity()
	{
		return security;
	}

	public BaseEntityExportBean getExportDetails()
	{
		return exportDetails;
	}

	public void setExportDetails(BaseEntityExportBean exportDetails)
	{
		this.exportDetails = exportDetails;
	}
}
