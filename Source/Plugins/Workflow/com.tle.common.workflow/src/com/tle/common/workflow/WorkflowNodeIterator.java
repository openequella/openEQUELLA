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

package com.tle.common.workflow;

import java.util.Iterator;

import com.tle.common.workflow.node.WorkflowNode;

public class WorkflowNodeIterator
{
	public WorkflowNodeIterator()
	{
		// Do nothing
	}

	public boolean iterate(Workflow workflow)
	{
		return iterate(workflow.getRoot());
	}

	public boolean iterate(WorkflowNode node)
	{
		boolean contains = false;
		for( Iterator<WorkflowNode> i = node.iterateChildren(); !contains && i.hasNext(); )
		{
			WorkflowNode n = i.next();
			if( n.canAddChildren() )
			{
				contains = iterate(n);
			}
			switch( n.getType() )
			{
				case WorkflowNode.ITEM_TYPE:
					contains = true;
					break;
				case WorkflowNode.DECISION_TYPE:
					contains = true;
					break;
				case WorkflowNode.PARALLEL_TYPE:
					contains = true;
					break;
				case WorkflowNode.SERIAL_TYPE:
					contains = true;
					break;
				case WorkflowNode.SCRIPT_TYPE:
					contains = true;
					break;

				default:
					// Shouldn't be anything else
					break;
			}
		}
		return contains;
	}
}
