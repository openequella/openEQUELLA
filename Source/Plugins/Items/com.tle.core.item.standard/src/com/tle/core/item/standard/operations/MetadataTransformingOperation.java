package com.tle.core.item.standard.operations;

import com.tle.core.item.operations.WorkflowOperation;

/**
 * @author aholland
 */
public interface MetadataTransformingOperation extends WorkflowOperation
{
	void setTransform(String transform);
}
