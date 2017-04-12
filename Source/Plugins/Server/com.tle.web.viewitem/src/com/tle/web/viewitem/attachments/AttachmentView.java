package com.tle.web.viewitem.attachments;

import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.equella.utils.AbstractCombinedRenderer;
import com.tle.web.viewurl.ViewableResource;

public class AttachmentView extends AbstractCombinedRenderer
{
	private final IAttachment attachment;
	private final ViewableResource viewableResource;
	private String overrideViewer;

	public AttachmentView(IAttachment attachment, ViewableResource resource)
	{
		this.attachment = attachment;
		this.viewableResource = resource;
	}

	public IAttachment getAttachment()
	{
		return attachment;
	}

	public ViewableResource getViewableResource()
	{
		return viewableResource;
	}

	public String getOverrideViewer()
	{
		return overrideViewer;
	}

	public void setOverrideViewer(String overrideViewer)
	{
		this.overrideViewer = overrideViewer;
	}
}
