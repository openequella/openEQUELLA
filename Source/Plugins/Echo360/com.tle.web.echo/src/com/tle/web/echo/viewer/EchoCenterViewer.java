package com.tle.web.echo.viewer;

import javax.inject.Singleton;

import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.echo.data.EchoAttachmentData;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
@SuppressWarnings("nls")
public class EchoCenterViewer extends AbstractEchoViewer
{
	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		IAttachment a = resource.getAttachment();
		if( a != null )
		{
			EchoAttachmentData ed = getEchoAttachmentData(a);
			boolean supportsMime = super.supports(info, resource);
			return ed == null ? supportsMime : supportsMime && !Check.isEmpty(ed.getEchoData().getEchoCenterUrl());
		}
		return super.supports(info, resource);
	}

	@Override
	public String getViewerId()
	{
		return "echoCenterViewer";
	}
}