package com.tle.web.controls.kaltura;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.kaltura.KalturaUtils;
import com.tle.core.guice.Bind;
import com.tle.core.kaltura.service.KalturaService;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.freetext.SupportedVideoMimeTypeExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;

@NonNullByDefault
@Bind
@Singleton
public class KalturaResource
	implements
		AttachmentResourceExtension<CustomAttachment>,
		RegisterMimeTypeExtension<CustomAttachment>,
		SupportedVideoMimeTypeExtension
{
	@Inject
	private SelectionService selection;
	@Inject
	private KalturaService kalturaService;
	@Inject
	private DateRendererFactory dateRendererFactory;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, CustomAttachment attachment)
	{
		return new KalturaViewableResource(resource, attachment, selection, kalturaService, info, dateRendererFactory);
	}

	@Override
	public String getMimeType(CustomAttachment attachment)
	{
		return KalturaUtils.MIME_TYPE;
	}

	@Override
	public boolean isSupportedMimeType(@Nullable String mimeType)
	{
		if( mimeType != null && mimeType.contains(KalturaUtils.MIME_TYPE) )
		{
			return true;
		}
		return false;
	}
}
