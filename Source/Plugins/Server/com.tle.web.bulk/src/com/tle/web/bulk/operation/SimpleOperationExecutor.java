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

package com.tle.web.bulk.operation;

import java.io.Serializable;

import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.FactoryMethodLocator;

public class SimpleOperationExecutor extends FactoryMethodLocator<WorkflowOperation> implements BulkOperationExecutor
{
	private static final long serialVersionUID = 1L;

	private final Class<? extends WorkflowOperation> operationClass;
	private final boolean save;

	public SimpleOperationExecutor(Class<? extends WorkflowOperation> operationClass, String methodName, boolean save,
		Serializable... args)
	{
		super(ItemOperationFactory.class, methodName, args);
		this.operationClass = operationClass;
		this.save = save;
	}

	@Override
	public WorkflowOperation[] getOperations()
	{
		ItemOperationFactory factory = getFactory();
		WorkflowOperation op = invokeFactoryMethod(factory);
		operationClass.cast(op);
		if( !save )
		{
			return new WorkflowOperation[]{op};
		}
		return new WorkflowOperation[]{op, factory.saveBackground()};
	}

	@Override
	public String getTitleKey()
	{
		return operationClass.getName();
	}
}
