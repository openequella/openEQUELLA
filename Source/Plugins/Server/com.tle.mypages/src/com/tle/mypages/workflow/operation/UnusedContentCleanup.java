package com.tle.mypages.workflow.operation;

import java.util.HashMap;
import java.util.Map;

import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.workflow.operations.DuringSaveOperation;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.mypages.workflow.operation.UnusedContentCleanupOperation.UnusedContentCleanupOperationFactory;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class UnusedContentCleanup extends FactoryMethodLocator<WorkflowOperation> implements DuringSaveOperation
{
	private static final long serialVersionUID = 1L;
	public static final String ID = "UNUSED_MYCONTENT";

	private final Map<String, String> metadataHtml = new HashMap<String, String>();

	public UnusedContentCleanup()
	{
		super(UnusedContentCleanupOperationFactory.class, "createWithContent");
	}

	@Override
	protected Object[] getArgs()
	{
		return new Object[]{metadataHtml.values()};
	}

	@Override
	public WorkflowOperation createPostSaveWorkflowOperation()
	{
		return null;
	}

	@Override
	public WorkflowOperation createPreSaveWorkflowOperation()
	{
		return get();
	}

	public void put(String key, String html)
	{
		metadataHtml.put(key, html);
	}

	@Override
	public String getName()
	{
		return null;
	}
}
