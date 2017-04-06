package com.tle.web.controls.googlebook;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;

@Bind
@Singleton
public class GoogleBookSearchResource
	implements
		AttachmentResourceExtension<CustomAttachment>,
		RegisterMimeTypeExtension<CustomAttachment>
{
	@Inject
	private SelectionService selection;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, CustomAttachment attachment)
	{
		return new GoogleBookSearchViewableResource(resource, attachment, selection, info);
	}

	@Override
	public String getMimeType(CustomAttachment attachment)
	{
		return GoogleBookConstants.MIME_TYPE;
	}

}
