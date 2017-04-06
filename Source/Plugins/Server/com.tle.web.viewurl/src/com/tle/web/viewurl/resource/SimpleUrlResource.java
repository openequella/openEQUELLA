package com.tle.web.viewurl.resource;

import java.util.List;

import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewableResource;

public class SimpleUrlResource extends AbstractWrappedResource
{
	private final String url;
	private final String description;
	private final boolean disabled;

	public SimpleUrlResource(ViewableResource resource, String url, String description, boolean disabled)
	{
		super(resource);
		this.url = url;
		this.description = description;
		this.disabled = disabled;
	}

	@Override
	public boolean hasContentStream()
	{
		return false;
	}

	@Override
	public String getMimeType()
	{
		return MimeTypeConstants.MIME_LINK;
	}

	@Override
	public boolean isExternalResource()
	{
		return true;
	}

	@Override
	public boolean isDisabled()
	{
		return disabled;
	}

	@Override
	public Bookmark createCanonicalUrl()
	{
		return new SimpleBookmark(url);
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public ViewAuditEntry getViewAuditEntry()
	{
		return new ViewAuditEntry("url", url); //$NON-NLS-1$
	}

	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		// See DetailUrlResource
		return null;
	}
}
