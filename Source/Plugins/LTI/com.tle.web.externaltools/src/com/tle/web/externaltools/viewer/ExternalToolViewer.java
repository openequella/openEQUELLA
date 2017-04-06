package com.tle.web.externaltools.viewer;

import com.google.inject.Singleton;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
public class ExternalToolViewer extends AbstractResourceViewer
{

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return resource.getMimeType().equals(ExternalToolConstants.MIME_TYPE);
	}

	@Override
	public String getViewerId()
	{
		return ExternalToolConstants.VIEWER_ID;
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return ExternalToolViewerSection.class;
	}
}
