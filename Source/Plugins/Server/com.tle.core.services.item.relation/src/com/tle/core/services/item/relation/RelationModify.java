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

package com.tle.core.services.item.relation;

import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.operations.DuringSaveOperation;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.services.item.relation.RelationOperation.RelationOperationFactory;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public class RelationModify extends FactoryMethodLocator<WorkflowOperation> implements DuringSaveOperation
{
	public static final String NAME = "editrelations";
	private static final long serialVersionUID = 1L;
	private final RelationOperationState state;

	public RelationModify(RelationOperationState state)
	{
		super(RelationOperationFactory.class, "create", state); //$NON-NLS-1$
		this.state = state;
	}

	public RelationOperationState getState()
	{
		return state;
	}

	@Override
	public WorkflowOperation createPostSaveWorkflowOperation()
	{
		return get();
	}

	@Override
	public WorkflowOperation createPreSaveWorkflowOperation()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return NAME;
	}
}
