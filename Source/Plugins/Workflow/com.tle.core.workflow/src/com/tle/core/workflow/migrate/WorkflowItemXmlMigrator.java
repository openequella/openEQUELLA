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

package com.tle.core.workflow.migrate;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;

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
