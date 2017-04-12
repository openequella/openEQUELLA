package com.tle.web.controls.externaltools;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.core.externaltools.service.ExternalToolsService;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;

/**
 * @author larry
 *
 */
@Bind
@Singleton
public class ExternalToolResource
	implements
		AttachmentResourceExtension<CustomAttachment>,
		RegisterMimeTypeExtension<CustomAttachment>
{

	@Inject
	private ExternalToolsService externalToolService;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, CustomAttachment attachment)
	{
		String iconUrl = externalToolService.findApplicableIconUrl(attachment);
		return new ExternalToolViewableResource(resource, iconUrl);
	}

	@Override
	public String getMimeType(CustomAttachment attachment)
	{
		return ExternalToolConstants.MIME_TYPE;
	}
}
