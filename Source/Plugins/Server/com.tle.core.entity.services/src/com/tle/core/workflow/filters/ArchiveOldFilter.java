/*
 * Created on Nov 5, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.filters;

import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemStatus;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public class ArchiveOldFilter extends BaseFilter
{
	private final ItemKey key;

	@AssistedInject
	protected ArchiveOldFilter(@Assisted ItemKey key)
	{
		this.key = key;
	}

	@Override
	public AbstractWorkflowOperation[] createOperations()
	{
		return new AbstractWorkflowOperation[]{workflowFactory.insecureArchive(), workflowFactory.save()};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("uuid", key.getUuid());
		values.put("status", ItemStatus.LIVE.name());
		values.put("version", key.getVersion());
	}

	@Override
	public String getWhereClause()
	{
		return "uuid = :uuid and status = :status and version < :version";
	}
}
