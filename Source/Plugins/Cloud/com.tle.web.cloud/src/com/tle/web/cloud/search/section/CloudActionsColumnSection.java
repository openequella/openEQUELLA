package com.tle.web.cloud.search.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.ResultListCollector;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
public class CloudActionsColumnSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return renderChildren(context, new ResultListCollector(true)).getFirstResult();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, TwoColumnLayout.RIGHT);
	}
}