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

package com.tle.core.item.convert;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;

@SuppressWarnings("nls")
public class WorkflowNodeConverter implements Converter
{
	private static final String ATTR_WORKFLOW = "workflow";

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		WorkflowNode node = (WorkflowNode) obj;
		writer.addAttribute(ATTR_WORKFLOW, node.getWorkflow().getUuid());
		writer.setValue(node.getUuid());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, final UnmarshallingContext context)
	{
		final WorkflowNodeSupplier supplier = (WorkflowNodeSupplier) context.get(WorkflowNodeSupplier.class);
		final String workflowUuid = reader.getAttribute(ATTR_WORKFLOW);
		final String nodeUuid = reader.getValue();
		long id = supplier.getIdForNode(workflowUuid, nodeUuid);
		final WorkflowItem node = new WorkflowItem();
		if( id != 0 )
		{
			node.setId(id);
		}
		else
		{
			node.setUuid(nodeUuid);
			context.addCompletionCallback(new Runnable()
			{
				@Override
				public void run()
				{
					node.setId(supplier.getIdForNode(workflowUuid, nodeUuid));
				}
			}, 1);
		}
		return node;
	}

	@Override
	public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz)
	{
		return WorkflowNode.class.isAssignableFrom(clazz);
	}

	public interface WorkflowNodeSupplier
	{
		long getIdForNode(String workflowUuid, String uuid);
	}
}