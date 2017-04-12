package com.tle.web.controls.echo;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.echo.service.EchoService;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.web.echo.EchoUtils;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;

@Bind
@Singleton
public class EchoResource
	implements
		AttachmentResourceExtension<CustomAttachment>,
		RegisterMimeTypeExtension<CustomAttachment>
{
	@Inject
	private SelectionService selection;
	@Inject
	private EchoService echoService;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, CustomAttachment attachment)
	{
		return new EchoViewableResource(resource, attachment, echoService, selection, info);
	}

	@Override
	public String getMimeType(CustomAttachment attachment)
	{
		return EchoUtils.MIME_TYPE;
	}
}
