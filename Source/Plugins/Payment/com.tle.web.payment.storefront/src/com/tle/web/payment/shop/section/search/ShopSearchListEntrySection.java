package com.tle.web.payment.shop.section.search;

import java.util.List;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.StandardListSection;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
@Bind
public class ShopSearchListEntrySection
	extends
		StandardListSection<ShopSearchListEntry, StandardListSection.Model<ShopSearchListEntry>>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	protected SectionRenderable getRenderable(RenderEventContext context)
	{
		return viewFactory.createResult("shop/list.ftl", this);
	}

	@Override
	public List<ShopSearchListEntry> initEntries(RenderContext context)
	{
		super.initEntries(context);
		List<ShopSearchListEntry> entries = getModel(context).getItems();
		// for( ShopSearchListEntry entry : entries )
		// {
		// entry.init();
		// }
		return entries;
	}
}
