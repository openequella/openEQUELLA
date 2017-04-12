package com.tle.qti;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.viewurl.ViewableResource;

@Bind
public class QTIViewer extends AbstractResourceViewer
{
	@Override
	public String getViewerId()
	{
		return "qti";
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return QTIViewerSection.class;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return resource.getMimeType().startsWith("text/xml");
	}
}
