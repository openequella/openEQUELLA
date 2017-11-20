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

package com.tle.web.api.item.interfaces.beans;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.interfaces.beans.UserBean;

@XmlRootElement
public class ItemBean extends AbstractExtendableBean
{
	private String uuid;
	private int version;
	private I18NString name;
	private I18NStrings nameStrings;
	private I18NString description;
	private I18NStrings descriptionStrings;
	private String metadata;
	private String status;
	private Date createdDate;
	private Date modifiedDate;
	private UserBean owner;
	private List<UserBean> collaborators;
	private BaseEntityReference collection;
	private Float rating;
	private List<AttachmentBean> attachments;
	private NavigationTreeBean navigation;
	private DrmBean drm;
	@JsonProperty(value = "_export")
	private ItemExportBean exportDetails;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public List<AttachmentBean> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(List<AttachmentBean> attachments)
	{
		this.attachments = attachments;
	}

	public String getMetadata()
	{
		return metadata;
	}

	public void setMetadata(String metadata)
	{
		this.metadata = metadata;
	}

	public UserBean getOwner()
	{
		return owner;
	}

	public void setOwner(UserBean owner)
	{
		this.owner = owner;
	}

	public List<UserBean> getCollaborators()
	{
		return collaborators;
	}

	public void setCollaborators(List<UserBean> collaborators)
	{
		this.collaborators = collaborators;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public Date getCreatedDate()
	{
		return createdDate;
	}

	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	public Date getModifiedDate()
	{
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	public NavigationTreeBean getNavigation()
	{
		return navigation;
	}

	public void setNavigation(NavigationTreeBean navigation)
	{
		this.navigation = navigation;
	}

	public Float getRating()
	{
		return rating;
	}

	public void setRating(Float rating)
	{
		this.rating = rating;
	}

	public DrmBean getDrm()
	{
		return drm;
	}

	public void setDrm(DrmBean drm)
	{
		this.drm = drm;
	}

	public BaseEntityReference getCollection()
	{
		return collection;
	}

	public void setCollection(BaseEntityReference collection)
	{
		this.collection = collection;
	}

	public I18NString getName()
	{
		return name;
	}

	public void setName(I18NString name)
	{
		this.name = name;
	}

	public I18NStrings getNameStrings()
	{
		return nameStrings;
	}

	public void setNameStrings(I18NStrings nameStrings)
	{
		this.nameStrings = nameStrings;
	}

	public I18NString getDescription()
	{
		return description;
	}

	public void setDescription(I18NString description)
	{
		this.description = description;
	}

	public I18NStrings getDescriptionStrings()
	{
		return descriptionStrings;
	}

	public void setDescriptionStrings(I18NStrings descriptionStrings)
	{
		this.descriptionStrings = descriptionStrings;
	}

	public ItemExportBean getExportDetails()
	{
		return exportDetails;
	}

	public void setExportDetails(ItemExportBean exportDetails)
	{
		this.exportDetails = exportDetails;
	}
}
