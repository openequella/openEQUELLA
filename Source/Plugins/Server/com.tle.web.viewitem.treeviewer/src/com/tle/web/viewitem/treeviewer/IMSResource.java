package com.tle.web.viewitem.treeviewer;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.common.Check;
import com.tle.common.FileSizeUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

public class IMSResource extends AbstractWrappedResource
{
	static
	{
		PluginResourceHandler.init(IMSResource.class);
	}

	@PlugKey("details.type")
	private static Label TYPE;
	@PlugKey("details.mimetype")
	private static Label MIMETYPE;
	@PlugKey("details.name")
	private static Label NAME;
	@PlugKey("details.scormversion")
	private static Label VERSION;
	@PlugKey("details.size")
	private static Label SIZE;

	public IMSResource(ViewableResource inner)
	{
		super(inner);
	}

	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();
		ImsAttachment attachment = (ImsAttachment) getAttachment();

		// Type
		commonDetails.add(makeDetail(TYPE, getMimeTypeLabel()));

		// Name
		commonDetails.add(makeDetail(NAME, new TextLabel(attachment.getDescription())));

		// Size
		commonDetails.add(makeDetail(SIZE, new TextLabel(FileSizeUtils.humanReadableFileSize(attachment.getSize()))));

		// SCORM version
		String version = (String) getAttachment().getData("SCORM_VERSION");
		if( !Check.isEmpty(version) )
		{
			commonDetails.add(makeDetail(VERSION, new TextLabel(version)));
		}

		return commonDetails;
	}

	@Override
	public String getFilepath()
	{
		return TreeNavigationSection.VIEWIMS_JSP;
	}

	protected Label getMimeTypeLabel()
	{
		return MIMETYPE;
	}
}