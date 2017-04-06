package com.tle.web.viewitem.externallinkviewer;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.viewurl.ViewableResource;

/**
 * @author aholland
 */
@Bind
@Singleton
public class ExternalLinkViewer extends AbstractResourceViewer
{
	@Override
	public String getViewerId()
	{
		return "externalLinkViewer"; //$NON-NLS-1$
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return ExternalLinkViewerSection.class;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return resource.getMimeType().equals(MimeTypeConstants.MIME_LINK);
	}
}
