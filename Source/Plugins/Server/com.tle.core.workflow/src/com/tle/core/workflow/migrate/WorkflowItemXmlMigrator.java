package com.tle.core.workflow.migrate;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.migration.AbstractItemXmlMigrator;

@Bind
@Singleton
public class WorkflowItemXmlMigrator extends AbstractItemXmlMigrator
{

	@SuppressWarnings("nls")
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		PropBagThoroughIterator iter = xml.iterateAll("moderation/statuses/*");
		boolean changed = false;
		while( iter.hasNext() )
		{
			PropBagEx status = iter.next();
			if( status.getNodeName().equals("com.tle.beans.entity.workflow.WorkflowItemStatus") )
			{
				status.setNodeName("com.tle.common.workflow.WorkflowItemStatus");
			}
			else
			{
				status.setNodeName("com.tle.common.workflow.WorkflowNodeStatus");
			}
			status.deleteNode("type");
			String nodeId = status.getNode("nodeId");
			status.deleteNode("nodeId");
			status.setNode("node", nodeId);
			changed = true;
		}
		return changed;
	}
}
