package com.tle.web.api.item.equella.interfaces.beans;

public class FileAttachmentBean extends AbstractFileAttachmentBean
{
	private static final String TYPE = "file";

	private String parentZip;
	private boolean conversion;

	public boolean isConversion()
	{
		return conversion;
	}

	public void setConversion(boolean conversion)
	{
		this.conversion = conversion;
	}

	/**
	 * @deprecated Use getThumbnail
	 */
	@Deprecated
	public String getThumbFilename()
	{
		return getThumbnail();
	}

	@Override
	public String getRawAttachmentType()
	{
		return TYPE;
	}

	public String getParentZip()
	{
		return parentZip;
	}

	public void setParentZip(String parentZip)
	{
		this.parentZip = parentZip;
	}

}
