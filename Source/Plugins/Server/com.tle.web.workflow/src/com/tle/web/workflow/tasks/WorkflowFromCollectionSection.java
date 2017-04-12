package com.tle.web.workflow.tasks;

import com.tle.common.workflow.Workflow;
import com.tle.core.guice.Bind;
import com.tle.web.search.filter.FilterByCollectionSection;
import com.tle.web.search.filter.FilterByCollectionSection.WhereEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.generic.AbstractPrototypeSection;

@Bind
public class WorkflowFromCollectionSection extends AbstractPrototypeSection<Object> implements WorkflowSelection
{
	@TreeLookup
	private FilterByCollectionSection filterByCollectionSection;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
	}

	@Override
	public Workflow getWorkflow(SectionInfo info)
	{
		WhereEntry entry = filterByCollectionSection.getCollectionList().getSelectedValue(info);
		if( entry == null || filterByCollectionSection.isDisabled(info) )
		{
			return null;
		}
		return entry.getEntity().getWorkflow();
	}

	@Override
	public void setWorkflow(SectionInfo info, Workflow workflow)
	{
		throw new UnsupportedOperationException();
	}
}
