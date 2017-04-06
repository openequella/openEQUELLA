package com.tle.web.echo.viewer;

import javax.inject.Inject;

import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.echo.service.EchoService;
import com.tle.web.echo.EchoUtils;
import com.tle.web.echo.data.EchoAttachmentData;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;

public abstract class AbstractEchoViewer extends AbstractResourceViewer
{
	@Inject
	private EchoService echoService;

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return resource.getMimeType().equals(EchoUtils.MIME_TYPE);
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return EchoViewerSection.class;
	}

	@Override
	public ViewItemViewer getViewer(SectionInfo info, ViewItemResource resource)
	{
		EchoViewerSection viewerSection = info.lookupSection(EchoViewerSection.class);
		viewerSection.setViewerId(info, getViewerId());
		return viewerSection;
	}

	protected EchoAttachmentData getEchoAttachmentData(IAttachment a)
	{
		EchoAttachmentData ed = null;
		try
		{
			ed = echoService.getMapper().readValue((String) a.getData(EchoUtils.PROPERTY_ECHO_DATA),
				EchoAttachmentData.class);

		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}

		return ed;
	}
}
