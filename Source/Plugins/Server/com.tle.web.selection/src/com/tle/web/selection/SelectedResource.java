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

package com.tle.web.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.viewurl.ItemUrlExtender;

public class SelectedResource implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final char TYPE_ATTACHMENT = 'a';
	public static final char TYPE_PATH = 'p';
	public static final char TYPE_FILE = 'f';
	public static final char TYPE_REMOTE = 'r';

	private final SelectedResourceKey key;
	private boolean versionChoiceMade;
	private boolean latest;
	private boolean previewResource;
	private String stagingId;
	private String title;
	private String description;
	private String viewerId;
	private long selectedDate;
	private List<ItemUrlExtender> extenders;
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public SelectedResource(SelectedResourceKey key)
	{
		selectedDate = System.currentTimeMillis();
		this.key = key;
	}

	/**
	 * Used by TinyMCE file upload...
	 */
	public SelectedResource()
	{
		this(new SelectedResourceKey());
	}

	public SelectedResource(ItemKey itemId, TargetFolder folder, String extensionType)
	{
		this(new SelectedResourceKey(itemId, folder, extensionType));
	}

	public SelectedResource(ItemKey itemId, IAttachment attachment, TargetFolder folder, String extensionType)
	{
		this(new SelectedResourceKey(itemId, attachment.getUuid(), folder, extensionType));
		title = attachment.getDescription();
		if( title == null )
		{
			title = attachment.getUrl();
		}
		description = ""; //$NON-NLS-1$
	}

	public void setAttribute(String key, Object value)
	{
		attributes.put(key, value);
	}

	public boolean isAttributeTrue(String key)
	{
		Boolean b = (Boolean) attributes.get(key);
		return (b != null && b);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String key)
	{
		return (T) attributes.get(key);
	}

	public ItemId createItemId()
	{
		return new ItemId(key.getUuid(), key.getVersion());
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getUrl()
	{
		return key.getUrl();
	}

	public void setUrl(String url)
	{
		key.setUrl(url);
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getViewerId()
	{
		return viewerId;
	}

	public void setViewerId(String viewerId)
	{
		this.viewerId = viewerId;
	}

	public void addExtender(ItemUrlExtender extender)
	{
		if( extenders == null )
		{
			extenders = new ArrayList<ItemUrlExtender>();
		}
		extenders.add(extender);
	}

	public List<ItemUrlExtender> getExtenders()
	{
		return extenders;
	}

	public long getSelectedDate()
	{
		return selectedDate;
	}

	public void setSelectedDate(long selectedDate)
	{
		this.selectedDate = selectedDate;
	}

	public boolean isVersionChoiceMade()
	{
		return versionChoiceMade;
	}

	public void setVersionChoiceMade(boolean versionChoiceMade)
	{
		this.versionChoiceMade = versionChoiceMade;
	}

	public boolean isLatest()
	{
		return latest;
	}

	public void setLatest(boolean latest)
	{
		this.latest = latest;
	}

	public SelectedResourceKey getKey()
	{
		return key;
	}

	public char getType()
	{
		return key.getType();
	}

	public String getUuid()
	{
		return key.getUuid();
	}

	public String getAttachmentUuid()
	{
		return key.getAttachmentUuid();
	}

	public int getVersion()
	{
		return key.getVersion();
	}

	public void setType(char type)
	{
		key.setType(type);
	}

	public boolean isPreviewResource()
	{
		return previewResource;
	}

	public void setPreviewResource(boolean previewResource)
	{
		this.previewResource = previewResource;
	}

	public void setPreviewId(String sessionId, String stagingId)
	{
		key.setUuid(sessionId);
		key.setVersion(1);
		this.stagingId = stagingId;
		previewResource = true;
	}

	public String getStagingId()
	{
		return stagingId;
	}
}
