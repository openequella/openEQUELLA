package com.tle.core.harvester.old;

import java.util.Date;

/**
 * Represents a Learning Object from a content repository.
 * 
 * @author Nicholas Read
 */
public class LearningObject
{
	protected String identifier;

	protected String title;
	protected Date created;
	protected Date modified;

	protected boolean attachment;
	protected boolean equellaItem;

	public LearningObject(String identifier, String title, Date created, boolean attachment)
	{
		this.identifier = identifier;
		this.title = title;
		this.created = created;
		this.attachment = attachment;
		this.equellaItem = false;
	}

	public Date getCreationDate()
	{
		return created;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getTitle()
	{
		return title;
	}

	/**
	 * @return Returns the attachments.
	 */
	public boolean hasAttachment()
	{
		return attachment;
	}

	public void setAttachment(boolean attachment)
	{
		this.attachment = attachment;
	}

	public void setModifiedDate(Date modified)
	{
		this.modified = modified;
	}

	public Date getModifiedDate()
	{
		return modified;
	}

	public boolean isEquellaItem()
	{
		return equellaItem;
	}

	public void isEquellaItem(boolean equellaItem)
	{
		this.equellaItem = equellaItem;
	}
}
