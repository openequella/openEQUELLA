package com.tle.web.remoterepo.section;

import java.util.List;

import com.tle.core.fedsearch.RemoteRepoSearchResult;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.AbstractListSection;
import com.tle.web.remoterepo.RemoteRepoListEntry;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
@Bind
public class RemoteRepoListSection<R extends RemoteRepoSearchResult>
	extends
		AbstractListSection<RemoteRepoListEntry<R>, AbstractListSection.Model<RemoteRepoListEntry<R>>>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	protected SectionRenderable getRenderable(RenderEventContext context)
	{
		return viewFactory.createResult("list.ftl", this); //$NON-NLS-1$
	}

	@Override
	protected List<RemoteRepoListEntry<R>> initEntries(RenderContext context)
	{
		return getModel(context).getItems();
	}
}