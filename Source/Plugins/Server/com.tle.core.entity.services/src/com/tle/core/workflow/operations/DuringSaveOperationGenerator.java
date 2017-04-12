package com.tle.core.workflow.operations;

import java.util.Collection;

/**
 * @author aholland Ok, the name is crap. I'll think of a better one later
 */
public interface DuringSaveOperationGenerator
{
	Collection<DuringSaveOperation> getDuringSaveOperation();
}
