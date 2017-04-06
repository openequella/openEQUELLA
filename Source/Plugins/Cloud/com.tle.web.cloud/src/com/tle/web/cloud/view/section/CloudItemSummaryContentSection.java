package com.tle.web.cloud.view.section;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.viewitem.summary.ItemSummaryContent;

/**
 * @author Aaron
 */
@Bind
public class CloudItemSummaryContentSection extends AbstractPrototypeSection<Object>
	implements
		ItemSummaryContent,
		HtmlRenderer
{
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final ResultListCollector results = new ResultListCollector(true);
		renderChildren(context, results);
		return new DivRenderer("area", results.getFirstResult());
	}
}
