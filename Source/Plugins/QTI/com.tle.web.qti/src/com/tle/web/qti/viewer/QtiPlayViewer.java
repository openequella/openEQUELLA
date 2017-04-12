package com.tle.web.qti.viewer;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.qti.QtiConstants;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
public class QtiPlayViewer extends AbstractResourceViewer
{
	static
	{
		PluginResourceHandler.init(QtiPlayViewer.class);
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return resource.getMimeType().equals(QtiConstants.TEST_MIME_TYPE);
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return QtiPlayViewerSection.class;
	}

	@SuppressWarnings("nls")
	@Override
	public String getViewerId()
	{
		return "qtiTestViewer";
	}

}
