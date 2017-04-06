package com.tle.web.viewitem.summary.section;

import java.util.Collection;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewitem.section.ResourceViewerAware;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;

@SuppressWarnings("nls")
@NonNullByDefault
public class SummarySection extends AbstractItemSummarySection<Item> implements ResourceViewerAware
{
	@PlugKey("pagetitle")
	private static Label PAGE_TITLE;

	@TreeLookup
	private ItemSummaryContentSection summarySection;
	@TreeLookup
	private RootItemFileSection rootSection;

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootSection.addViewerMapping(Type.FULL, this, "");
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return DISCOVER_AND_VIEW_PRIVS;
	}

	@Override
	protected boolean isPreview(SectionInfo info)
	{
		return ParentViewItemSectionUtils.isForPreview(info);
	}

	@Override
	protected String getContentBodyClass(SectionInfo info)
	{
		return "itemsummary-layout";
	}

	@Override
	protected Item getItem(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info).getItem();
	}

	@Override
	public void beforeRender(SectionInfo info, ViewItemResource resource)
	{
		summarySection.ensureTree(info, resource);
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return new ViewAuditEntry(true);
	}

	@Override
	protected Label getPageTitle(SectionInfo info)
	{
		return PAGE_TITLE;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "summ";
	}

	@Override
	public Class<TwoColumnModel> getModelClass()
	{
		return TwoColumnModel.class;
	}
}
