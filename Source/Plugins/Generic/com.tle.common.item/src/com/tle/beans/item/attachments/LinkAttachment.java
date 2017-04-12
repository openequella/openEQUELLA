/*
 * Created on Aug 4, 2005
 */
package com.tle.beans.item.attachments;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
@DiscriminatorValue("attachment")
public class LinkAttachment extends Attachment
{
	private static final long serialVersionUID = 1L;

	public LinkAttachment()
	{
		super();
	}

	@Override
	public AttachmentType getAttachmentType()
	{
		return AttachmentType.LINK;
	}
}
