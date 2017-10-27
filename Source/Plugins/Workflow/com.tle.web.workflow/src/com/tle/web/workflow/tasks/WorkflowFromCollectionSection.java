/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.workflow.tasks;

import com.tle.common.workflow.Workflow;
import com.tle.core.guice.Bind;
import com.tle.web.search.filter.AbstractFilterByCollectionSection.WhereEntry;
import com.tle.web.search.filter.FilterByCollectionSection;
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
