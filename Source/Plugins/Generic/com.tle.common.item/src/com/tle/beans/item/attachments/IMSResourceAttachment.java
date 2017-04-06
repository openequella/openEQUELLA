package com.tle.beans.item.attachments;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
@DiscriminatorValue("imsres")
public class IMSResourceAttachment extends Attachment
{
	private static final long serialVersionUID = 1L;

	@Override
	public AttachmentType getAttachmentType()
	{
		return AttachmentType.IMSRES;
	}

}
