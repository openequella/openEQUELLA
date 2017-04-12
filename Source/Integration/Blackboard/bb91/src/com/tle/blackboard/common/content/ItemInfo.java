package com.tle.blackboard.common.content;

import java.util.Date;

/**
 * @author Aaron
 */
// @NonNullByDefault
public class ItemInfo
{
	private final ItemKey itemKey;
	/* @Nullable */
	private String name;
	/* @Nullable */
	private String description;
	/* @Nullable */
	private String activateRequestUuid;
	/* @Nullable */
	private Date createdDate;
	/* @Nullable */
	private Date modifiedDate;
	/* @Nullable */
	private Date dateAccessed;
	private boolean available;
	/* @Nullable */
	private String attachmentName;
	/* @Nullable */
	private String mimeType;

	public ItemInfo(ItemKey itemKey)
	{
		this.itemKey = itemKey;
	}

	public ItemInfo(String institutionUrl, String itemUuid, int itemVersion, String contentId, String courseId,
		String folderId, String page)
	{
		itemKey = new ItemKey(institutionUrl, itemUuid, itemVersion, contentId, courseId, folderId, page);
	}

	public ItemKey getItemKey()
	{
		return itemKey;
	}

	/* @Nullable */
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/* @Nullable */
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	/* @Nullable */
	public String getActivateRequestUuid()
	{
		return activateRequestUuid;
	}

	public void setActivateRequestUuid(String activateRequestUuid)
	{
		this.activateRequestUuid = activateRequestUuid;
	}

	/* @Nullable */
	public Date getCreatedDate()
	{
		return createdDate;
	}

	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	/* @Nullable */
	public Date getModifiedDate()
	{
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	public boolean isAvailable()
	{
		return available;
	}

	public void setAvailable(boolean available)
	{
		this.available = available;
	}

	/* @Nullable */
	public Date getDateAccessed()
	{
		return dateAccessed;
	}

	public void setDateAccessed(Date dateAccessed)
	{
		this.dateAccessed = dateAccessed;
	}

	public String getAttachmentName()
	{
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName)
	{
		this.attachmentName = attachmentName;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}
}
