/**
 *
 */
package com.tle.web.controls.flickr;

import java.util.List;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.AbstractListSection;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author larry
 */
@Bind
@SuppressWarnings("nls")
public class FlickrListSection extends AbstractListSection<FlickrListEntry, AbstractListSection.Model<FlickrListEntry>>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	protected SectionRenderable getRenderable(RenderEventContext context)
	{
		return viewFactory.createResult("flickrlist.ftl", this);
	}

	@Override
	protected List<FlickrListEntry> initEntries(RenderContext context)
	{
		return getModel(context).getItems();
	}
}
