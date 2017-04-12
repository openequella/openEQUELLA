package com.tle.web.bulk.operation;

import it.uniroma3.mat.extendedset.wrappers.LongSet;

import java.util.Collection;

import com.tle.beans.item.ItemKey;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.services.impl.ClusteredTask;

public interface BulkOperationService
{
	String KEY_OPERATION_TITLE = "operationTitle"; //$NON-NLS-1$

	ClusteredTask createTask(Collection<? extends ItemKey> items, BeanLocator<? extends BulkOperationExecutor> executor);

	ClusteredTask createTask(LongSet items, BeanLocator<? extends BulkOperationExecutor> executor);
}
