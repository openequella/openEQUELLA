package com.tle.web.searching.itemlist;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.item.StandardItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

@SuppressWarnings("nls")
@Bind
public class VideoItemList extends StandardItemList
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	public static final String VIDEO_FLAG = "video.result";

	@Override
	protected SectionRenderable getRenderable(RenderEventContext context)
	{
		return viewFactory.createResult("videolist.ftl", this);
	}

	@Override
	protected void customiseListEntries(RenderContext context, List<StandardItemListEntry> entries)
	{
		getListSettings(context).setAttribute(VIDEO_FLAG, true);
		super.customiseListEntries(context, entries);
	}

	@Override
	protected Set<String> getExtensionTypes()
	{
		return Collections.singleton("video");
	}
}
