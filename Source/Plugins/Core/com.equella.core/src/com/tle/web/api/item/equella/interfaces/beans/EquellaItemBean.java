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

package com.tle.web.api.item.equella.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.item.interfaces.beans.ItemBean;

@SuppressWarnings("nls")
@XmlRootElement
public class EquellaItemBean extends ItemBean
{
	private String thumbnail;

	public String getThumbnail()
	{
		return thumbnail;
	}

	public void setThumbnail(String thumbnail)
	{
		this.thumbnail = thumbnail;
	}

	/**
	 * Urggh. This needs to go when I work out how to change the default mapper
	 * to go to EqIBean
	 * 
	 * @param bean
	 * @return
	 */
	public static EquellaItemBean copyFrom(ItemBean bean)
	{
		EquellaItemBean eb = new EquellaItemBean();
		eb.setUuid(bean.getUuid());
		eb.setVersion(bean.getVersion());
		eb.setAttachments(bean.getAttachments());
		eb.setCollaborators(bean.getCollaborators());
		eb.setCollection(bean.getCollection());
		eb.setCreatedDate(bean.getCreatedDate());
		eb.setDescription(bean.getDescription());
		eb.setDescriptionStrings(bean.getDescriptionStrings());
		eb.setDrm(bean.getDrm());
		eb.setExportDetails(bean.getExportDetails());
		eb.setMetadata(bean.getMetadata());
		eb.setModifiedDate(bean.getModifiedDate());
		eb.setName(bean.getName());
		eb.setNameStrings(bean.getNameStrings());
		eb.setNavigation(bean.getNavigation());
		eb.setOwner(bean.getOwner());
		eb.setRating(bean.getRating());
		eb.setStatus(bean.getStatus());
		eb.setThumbnail((String) bean.get("thumbnail"));
		return eb;
	}
}
