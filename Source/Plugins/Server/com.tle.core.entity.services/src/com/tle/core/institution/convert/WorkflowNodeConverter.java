package com.tle.core.institution.convert;

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