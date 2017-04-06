package com.tle.web.mimetypes.search.result;

import java.util.List;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.AbstractListSection;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

@Bind
public class MimeListEntrySection extends AbstractListSection<MimeListEntry, AbstractListSection.Model<MimeListEntry>>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	protected SectionRenderable getRenderable(RenderEventContext context)
	{
		return viewFactory.createResult("list.ftl", this); //$NON-NLS-1$
	}

	@Override
	protected List<MimeListEntry> initEntries(RenderContext context)
	{
		List<MimeListEntry> entries = getModel(context).getItems();
		for( MimeListEntry mimeListEntry : entries )
		{
			mimeListEntry.init();
		}
		return entries;
	}
}
