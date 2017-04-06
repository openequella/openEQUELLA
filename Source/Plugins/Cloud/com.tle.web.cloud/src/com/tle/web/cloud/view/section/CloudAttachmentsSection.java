package com.tle.web.cloud.view.section;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.cloud.CloudConstants;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.guice.Bind;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.Label;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.AttachmentViewFilter;
import com.tle.web.viewitem.summary.attachment.service.ViewAttachmentWebService.AttachmentRowDisplay;
import com.tle.web.viewitem.summary.section.attachment.AbstractAttachmentsSection;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
public class CloudAttachmentsSection
	extends
		AbstractAttachmentsSection<CloudItem, CloudAttachmentsSection.CloudAttachmentsModel>
{
	@PlugKey("viewitem.attachments.title")
	private static Label LABEL_ATTACHMENTS_TITLE;

	@Override
	protected boolean showFullscreen(SectionInfo info, CloudItem item, List<AttachmentRowDisplay> rows)
	{
		return false;
	}

	@Nullable
	@Override
	protected Bookmark getFullscreenBookmark(SectionInfo info, ViewableItem<CloudItem> vitem)
	{
		return null;
	}

	@Override
	protected ViewableItem<CloudItem> getViewableItem(SectionInfo info)
	{
		return CloudItemSectionInfo.getItemInfo(info).getViewableItem();
	}

	@Override
	protected String getItemExtensionType()
	{
		return CloudConstants.ITEM_EXTENSION;
	}

	@Nullable
	@Override
	protected JSCallable getSelectPackageFunction(SectionInfo info, ViewableItem<CloudItem> vitem)
	{
		return null;
	}

	@Override
	protected Label getTitle(SectionInfo info, ViewableItem<CloudItem> vitem)
	{
		return LABEL_ATTACHMENTS_TITLE;
	}

	@Override
	protected AttachmentViewFilter getCustomFilter(SectionInfo info, ViewableItem<CloudItem> vitem)
	{
		return null;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new CloudAttachmentsModel();
	}

	public static class CloudAttachmentsModel extends AbstractAttachmentsSection.AttachmentsModel
	{
		// Nothing specific
	}

	@Override
	protected String getAttchmentControlId()
	{
		return null;
	}
}
