package com.tle.web.cloud.viewable;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface CloudViewItemUrlFactory
{
	CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem);

	CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem, int flags);

	CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem, CloudAttachment cloudAttachment);

	CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem, CloudAttachment cloudAttachment,
		int flags);
}
