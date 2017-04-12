/*
 * Created on Aug 4, 2005
 */
package com.tle.beans.item.attachments;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.common.Check;

@Entity
@AccessType("field")
@DiscriminatorValue("ims")
@SuppressWarnings("nls")
public class ImsAttachment extends Attachment
{
	private static final long serialVersionUID = 1L;
	private static final String KEY_EXPAND_IMS_PACKAGE = "EXPAND_IMS_PACKAGE";

	public ImsAttachment()
	{
		super();
	}

	@Override
	public AttachmentType getAttachmentType()
	{
		return AttachmentType.IMS;
	}

	public long getSize()
	{
		return getLongValue(value1, 0L);
	}

	public void setSize(long size)
	{
		value1 = String.valueOf(size);
	}

	public String getScormVersion()
	{
		return value2;
	}

	public void setScormVersion(String scorm)
	{
		value2 = scorm;
	}

	public void setExpand(boolean expand)
	{
		setData(KEY_EXPAND_IMS_PACKAGE, expand);
	}

	public boolean isExpand()
	{
		Object expand = getData(KEY_EXPAND_IMS_PACKAGE);
		if( expand == null || ((Boolean) expand) )
		{
			return true;
		}

		return false;
	}

	public boolean isScorm()
	{
		return !Check.isEmpty(getScormVersion());
	}
}
