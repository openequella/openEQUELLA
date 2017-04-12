package com.tle.web.connectors.service;

import java.util.Collection;

import com.tle.beans.item.ItemKey;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.services.impl.ClusteredTask;
import com.tle.web.bulk.operation.BulkOperationExecutor;

public interface ConnectorBulkOperationService
{
	@SuppressWarnings("nls")
	String KEY_OPERATION_TITLE = "operationTitle";

	ClusteredTask createTask(Collection<? extends ItemKey> items, BeanLocator<? extends BulkOperationExecutor> executor);
}
