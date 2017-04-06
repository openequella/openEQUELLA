package com.tle.web.scorm;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.FileSizeUtils;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.scorm.treeviewer.ScormTreeNavigationSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

@Bind
@Singleton
public class ScormResource
	implements
		AttachmentResourceExtension<CustomAttachment>,
		RegisterMimeTypeExtension<CustomAttachment>
{
	@PlugKey("details.type")
	private static KeyLabel TYPE;
	@PlugKey("details.mimetype")
	private static KeyLabel MIMETYPE;
	@PlugKey("details.name")
	private static Label NAME;
	@PlugKey("details.size")
	private static Label SIZE;

	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ViewItemUrlFactory urlFactory;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, CustomAttachment attachment)
	{

		ViewableResource res = attachmentResourceService.createPathResource(info, resource.getViewableItem(),
			ScormTreeNavigationSection.VIEWSCORM_JSP, attachment.getDescription(), MimeTypeConstants.MIME_SCORM,
			attachment);
		return new ScormViewableResource(res);
	}

	public class ScormViewableResource extends AbstractWrappedResource
	{

		public ScormViewableResource(ViewableResource inner)
		{
			super(inner);
		}

		@Override
		public List<AttachmentDetail> getCommonAttachmentDetails()
		{
			List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();
			CustomAttachment attachment = (CustomAttachment) getAttachment();

			// Type
			commonDetails.add(makeDetail(TYPE, MIMETYPE));

			// Name
			commonDetails.add(makeDetail(NAME, new TextLabel(attachment.getDescription())));

			Object size = attachment.getData("fileSize"); //$NON-NLS-1$
			if( size != null )
			{
				// Size
				commonDetails.add(makeDetail(SIZE, new TextLabel(FileSizeUtils.humanReadableFileSize((Long) size))));
			}

			return commonDetails;
		}

		@Override
		public ViewItemUrl createDefaultViewerUrl()
		{
			ViewItemUrl vurl = urlFactory.createItemUrl(inner.getInfo(), inner.getViewableItem(),
				UrlEncodedString.createFromValue(getFilepath()), 0);
			vurl.addFlag(ViewItemUrl.FLAG_NO_SELECTION);
			return vurl;
		}

		@Override
		public boolean isExternalResource()
		{
			return true;
		}

		@Override
		public String getFilepath()
		{
			return ScormTreeNavigationSection.VIEWSCORM_JSP;
		}
	}

	@Override
	public String getMimeType(CustomAttachment attachment)
	{
		return ScormUtils.MIME_TYPE;
	}
}
