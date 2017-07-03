package com.tle.core.item.standard.operations;

import java.util.Collection;

/**
 * @author aholland Ok, the name is crap. I'll think of a better one later
 */
public interface DuringSaveOperationGenerator
{
	Collection<DuringSaveOperation> getDuringSaveOperation();
}
