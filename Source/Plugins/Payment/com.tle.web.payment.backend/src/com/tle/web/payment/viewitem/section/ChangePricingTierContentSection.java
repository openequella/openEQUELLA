package com.tle.web.payment.viewitem.section;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class ChangePricingTierContentSection extends AbstractContentSection<Object>
{
	@Inject
	private ChangePricingTierSection changePricingTierSection;

	@PlugKey("viewitem.tier.action")
	private static Label LINK_LABEL;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
		addDefaultBreadcrumbs(context, itemInfo, LINK_LABEL);

		return renderSection(context, changePricingTierSection);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(changePricingTierSection, id);
	}
}
