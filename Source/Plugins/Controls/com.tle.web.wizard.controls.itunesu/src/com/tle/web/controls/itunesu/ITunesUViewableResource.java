package com.tle.web.controls.itunesu;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

public class ITunesUViewableResource extends AbstractWrappedResource
{
	static
	{
		PluginResourceHandler.init(ITunesUViewableResource.class);
	}

	@PlugKey("details.type")
	private static Label TYPE;
	@PlugKey("details.mimetype")
	private static Label MIMETYPE;
	@PlugKey("details.name")
	private static Label NAME;

	private final CustomAttachment itunesAttachment;

	public ITunesUViewableResource(ViewableResource resource, CustomAttachment attachment)
	{
		super(resource);
		this.itunesAttachment = attachment;
	}

	@Override
	public boolean hasContentStream()
	{
		return false;
	}

	@Override
	public ViewAuditEntry getViewAuditEntry()
	{
		return new ViewAuditEntry("itunesu", getTrackUrl()); //$NON-NLS-1$
	}

	@Override
	public String getMimeType()
	{
		return "equella/attachment-itunesu"; //$NON-NLS-1$
	}

	@Override
	public boolean isExternalResource()
	{
		return true;
	}

	@Override
	public Bookmark createCanonicalUrl()
	{
		return new SimpleBookmark(getTrackUrl());
	}

	private String getTrackUrl()
	{
		return (String) itunesAttachment.getData("trackUrl"); //$NON-NLS-1$
	}

	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();
		IAttachment attachment = getAttachment();

		// Type
		commonDetails.add(makeDetail(TYPE, MIMETYPE));

		// Name (real track name if available, otherwise display name)
		String name = (String) attachment.getData("trackName");
		commonDetails.add(makeDetail(NAME, new TextLabel(!Check.isEmpty(name) ? name : attachment.getDescription())));

		return commonDetails;
	}
}