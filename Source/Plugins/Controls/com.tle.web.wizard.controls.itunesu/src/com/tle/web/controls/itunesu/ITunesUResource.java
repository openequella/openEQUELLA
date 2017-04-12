package com.tle.web.controls.itunesu;

import javax.inject.Singleton;

import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ITunesUResource
	implements
		AttachmentResourceExtension<CustomAttachment>,
		RegisterMimeTypeExtension<CustomAttachment>
{
	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, CustomAttachment attachment)
	{
		return new ITunesUViewableResource(resource, attachment);
	}

	@Override
	public String getMimeType(CustomAttachment attachment)
	{
		return "equella/attachment-itunesu";
	}
}
