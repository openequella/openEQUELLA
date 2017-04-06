package com.tle.web.viewitem.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.viewurl.ItemSectionInfo;

@NonNullByDefault
public abstract class AbstractParentViewItemSection<M> extends AbstractPrototypeSection<M>
	implements
		ViewableChildInterface,
		HtmlRenderer
{
	@ViewFactory(fixed = false, optional = true)
	protected FreemarkerFactory viewFactory;

	public static ItemSectionInfo getItemInfo(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info);
	}

	protected boolean canViewChildren(SectionInfo info)
	{
		return SectionUtils.canViewChildren(info, this);
	}

	protected boolean isForPreview(SectionInfo info)
	{
		return ParentViewItemSectionUtils.isForPreview(info);
	}

	protected boolean isInIntegration(SectionInfo info)
	{
		return ParentViewItemSectionUtils.isInIntegration(info);
	}
}
