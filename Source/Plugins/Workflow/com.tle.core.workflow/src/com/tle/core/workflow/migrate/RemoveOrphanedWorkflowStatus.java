package com.tle.core.workflow.migrate;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Singleton;

import com.tle.beans.item.ModerationStatus;
import com.tle.common.Check;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;

@Bind
@Singleton
public class RemoveOrphanedWorkflowStatus implements PostReadMigrator<ItemConverterInfo>
{
	@Override
	public void migrate(ItemConverterInfo obj) throws IOException
	{
		ModerationStatus moderation = obj.getItem().getModeration();
		if( moderation != null )
		{
			Set<WorkflowNodeStatus> statuses = moderation.getStatuses();
			if( !Check.isEmpty(statuses) )
			{
				Iterator<WorkflowNodeStatus> iter = statuses.iterator();
				while( iter.hasNext() )
				{
					WorkflowNodeStatus status = iter.next();
					if( status.getNode() == null )
					{
						iter.remove();
					}
				}
			}
		}
	}
}