package com.tle.beans.item.attachments;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
@DiscriminatorValue("custom")
public class CustomAttachment extends Attachment
{
	private static final long serialVersionUID = 1L;

	public CustomAttachment()
	{
		super();
	}

	public void setType(String type)
	{
		value1 = type;
	}

	public String getType()
	{
		return value1;
	}

	@Override
	public AttachmentType getAttachmentType()
	{
		return AttachmentType.CUSTOM;
	}
}
