package com.tle.core.workflow.operations;

/**
 * @author aholland
 */
public interface MetadataTransformingOperation extends WorkflowOperation
{
	void setTransform(String transform);
}
