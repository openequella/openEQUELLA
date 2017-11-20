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

package com.tle.core.cloud.beans.converted;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.tle.annotation.NonNull;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.IItemNavigationNode;
import com.tle.common.i18n.LangUtils;
import com.tle.common.util.UnmodifiableIterable;

/**
 * @author Aaron
 */
public class CloudItem implements IItem<CloudAttachment>
{
	private final String uuid;
	private final int version;

	private Map<String, String> nameStrings;
	private Map<String, String> descriptionStrings;
	private List<CloudAttachment> attachments;
	private List<CloudItemNavigationNode> treeNodes;
	private CloudNavigationSettings navigationSettings;
	private String metadata;
	private Date createdDate;
	private Date modifiedDate;

	public CloudItem(@NonNull String uuid, int version)
	{
		this.uuid = uuid;
		this.version = version;
	}

	public void setNameStrings(Map<String, String> nameStrings)
	{
		this.nameStrings = nameStrings;
	}

	public void setDescriptionStrings(Map<String, String> descriptionStrings)
	{
		this.descriptionStrings = descriptionStrings;
	}

	@NonNull
	@Override
	public String getUuid()
	{
		return uuid;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	public void setAttachments(List<CloudAttachment> attachments)
	{
		this.attachments = attachments;
	}

	@Override
	public List<CloudAttachment> getAttachments()
	{
		return attachments;
	}

	@Override
	public UnmodifiableIterable<CloudAttachment> getAttachmentsUnmodifiable()
	{
		return new UnmodifiableIterable<CloudAttachment>(attachments);
	}

	@NonNull
	@Override
	public ItemId getItemId()
	{
		return new ItemId(uuid, version);
	}

	@Override
	public List<? extends IItemNavigationNode> getTreeNodes()
	{
		return treeNodes;
	}

	public void setTreeNodes(List<CloudItemNavigationNode> treeNodes)
	{
		this.treeNodes = treeNodes;
	}

	@Override
	public LanguageBundle getName()
	{
		return LangUtils.createTextTempLangugageBundle(nameStrings);
	}

	public void setNavigationSettings(CloudNavigationSettings navigationSettings)
	{
		this.navigationSettings = navigationSettings;
	}

	@Override
	public CloudNavigationSettings getNavigationSettings()
	{
		return navigationSettings;
	}

	@Override
	public LanguageBundle getDescription()
	{
		return LangUtils.createTextTempLangugageBundle(descriptionStrings);
	}

	public String getMetadata()
	{
		return metadata;
	}

	public void setMetadata(String metadata)
	{
		this.metadata = metadata;
	}

	@Override
	public float getRating()
	{
		return 0;
	}

	@Override
	public Date getDateCreated()
	{
		return createdDate;
	}

	public void setDateCreated(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	@Override
	public Date getDateModified()
	{
		return modifiedDate;
	}

	public void setDateModified(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}
}
