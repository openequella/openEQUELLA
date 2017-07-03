package com.tle.core.item.standard.filter;

import javax.inject.Inject;

import com.tle.core.item.operations.BaseFilter;
import com.tle.core.item.standard.ItemOperationFactory;

/**
 * @author Aaron
 *
 */
public abstract class AbstractStandardOperationFilter extends BaseFilter
{
	@Inject
	protected ItemOperationFactory operationFactory;
}
