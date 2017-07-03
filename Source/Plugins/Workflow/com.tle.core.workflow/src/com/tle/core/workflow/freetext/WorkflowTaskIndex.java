/*
 * Created on Aug 31, 2005
 */
package com.tle.core.workflow.freetext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.lucene.document.Document;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.searching.Search;
import com.tle.core.freetext.index.MultipleIndex;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.TaskResult;

@Bind
@Singleton
public class WorkflowTaskIndex extends MultipleIndex<TaskResult>
{
	@Override
	protected Set<String> getKeyFields()
	{
		return new HashSet<String>(Arrays.asList(FreeTextQuery.FIELD_UNIQUE, FreeTextQuery.FIELD_ID,
			FreeTextQuery.FIELD_WORKFLOW_TASKID));
	}

	@Override
	public String getIndexId()
	{
		return Search.INDEX_TASK;
	}

	@Override
	protected TaskResult createResult(ItemIdKey key, Document doc, float relevance, boolean sortByRelevance)
	{
		return new TaskResult(key, doc.get(FreeTextQuery.FIELD_WORKFLOW_TASKID), relevance, sortByRelevance);
	}
}
