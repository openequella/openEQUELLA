package com.tle.web.search.filter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public abstract class AbstractResetFiltersQuerySection<M, SE extends AbstractSearchEvent<SE>>
	extends
		AbstractQuerySection<M, SE> implements ResetFiltersParent
{
	@Inject
	private ResetFiltersSection<?> resetFiltersSection;

	protected final List<SectionId> queryActionSections = new ArrayList<SectionId>();

	@Override
	public ResetFiltersSection<?> getResetFiltersSection()
	{
		return resetFiltersSection;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(resetFiltersSection, id);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		queryActionSections.addAll(tree.getChildIds(id));
	}
	
	@Override
	public void addResetDiv(SectionTree tree, List<String> ajaxList)
	{
		String ajaxDiv = getAjaxDiv();
		if( ajaxDiv == null || !ajaxList.contains(ajaxDiv) )
		{
			resetFiltersSection.addAjaxDiv(ajaxList);
		}
	}

	protected void renderQueryActions(RenderEventContext context, AbstractQuerySectionModel model)
	{
		model.setQueryActions(SectionUtils.renderSectionIds(context, queryActionSections));
	}

	@Nullable
	protected String getAjaxDiv()
	{
		return null;
	}

	public static class AbstractQuerySectionModel
	{
		private List<SectionRenderable> queryActions;

		public List<SectionRenderable> getQueryActions()
		{
			return queryActions;
		}

		public void setQueryActions(List<SectionRenderable> queryActions)
		{
			this.queryActions = queryActions;
		}
	}
}
