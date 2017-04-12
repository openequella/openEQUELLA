package com.tle.web.controls.flickr;

import javax.inject.Inject;

import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;

/**
 * @author Larry. Based on the YouTube plugin.
 */
@Bind
public class FlickrResource
	implements
		AttachmentResourceExtension<CustomAttachment>,
		RegisterMimeTypeExtension<CustomAttachment>
{
	@Inject
	private SelectionService selection;
	@Inject
	private DateRendererFactory dateRendererFactory;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, CustomAttachment attachment)
	{
		return new FlickrViewableResource(resource, attachment, selection, info, dateRendererFactory);
	}

	@Override
	public String getMimeType(CustomAttachment attachment)
	{
		return FlickrUtils.MIME_TYPE;
	}
}
