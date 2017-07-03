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

package com.tle.web.itemadmin.operation;

import java.io.Serializable;

import com.tle.core.item.operations.WorkflowOperation;
import com.tle.web.bulk.operation.SimpleOperationExecutor;

public final class StandardOperation
{
	private final Class<? extends WorkflowOperation> opClass;
	private final String operationId;
	private final boolean save;
	private final String methodName;
	private final Serializable[] args;

	public StandardOperation(String operationId, Class<? extends WorkflowOperation> opClass, String methodName)
	{
		this(operationId, opClass, methodName, true);
	}

	public StandardOperation(String operationId, Class<? extends WorkflowOperation> opClass, String methodName,
		boolean save, Serializable... args)
	{
		this.operationId = operationId;
		this.opClass = opClass;
		this.save = save;
		this.methodName = methodName;
		this.args = args;
	}

	public SimpleOperationExecutor getExecutor()
	{
		return new SimpleOperationExecutor(opClass, methodName, save, args);
	}

	public String getOperationId()
	{
		return operationId;
	}
}