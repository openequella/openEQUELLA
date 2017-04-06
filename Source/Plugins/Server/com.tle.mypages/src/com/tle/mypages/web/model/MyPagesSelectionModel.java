package com.tle.mypages.web.model;

import java.util.List;

import com.tle.web.viewitem.attachments.AttachmentView;

/**
 * @author aholland
 */
public class MyPagesSelectionModel
{
	private List<AttachmentView> attachments;
	private boolean selectMultiple;

	public List<AttachmentView> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(List<AttachmentView> attachments)
	{
		this.attachments = attachments;
	}

	public boolean isSelectMultiple()
	{
		return selectMultiple;
	}

	public void setSelectMultiple(boolean selectMultiple)
	{
		this.selectMultiple = selectMultiple;
	}
}
