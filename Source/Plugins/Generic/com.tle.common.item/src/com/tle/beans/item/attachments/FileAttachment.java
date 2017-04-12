/*
 * Created on Aug 4, 2005
 */
package com.tle.beans.item.attachments;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
@DiscriminatorValue("file")
public class FileAttachment extends Attachment
{
	private static final long serialVersionUID = 1L;

	@Override
	public AttachmentType getAttachmentType()
	{
		return AttachmentType.FILE;
	}

	/**
	 * Equivilent to <code>getUrl</code>.
	 */
	public String getFilename()
	{
		return getUrl();
	}

	/**
	 * Equivilent to <code>setUrl</code>.
	 */
	public void setFilename(String filename)
	{
		setUrl(filename);
	}

	public long getSize()
	{
		return getLongValue(value1, 0L);
	}

	public void setSize(long size)
	{
		value1 = String.valueOf(size);
	}

	public boolean isConversion()
	{
		return getBooleanValue(value2);
	}

	public void setConversion(boolean conversion)
	{
		value2 = String.valueOf(conversion);
	}

	@Override
	public void setThumbnail(String thumbnail)
	{
		value3 = null;
		super.setThumbnail(thumbnail);
	}

	@Override
	public String getThumbnail()
	{
		if( thumbnail == null )
		{
			return value3;
		}
		return super.getThumbnail();
	}

	@Override
	public String toString()
	{
		return getFilename();
	}
}
