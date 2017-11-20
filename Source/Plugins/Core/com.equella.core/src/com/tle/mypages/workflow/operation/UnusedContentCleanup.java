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

package com.tle.mypages.workflow.operation;

import java.util.HashMap;
import java.util.Map;

import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.operations.DuringSaveOperation;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.mypages.workflow.operation.UnusedContentCleanupOperation.UnusedContentCleanupOperationFactory;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class UnusedContentCleanup extends FactoryMethodLocator<WorkflowOperation> implements DuringSaveOperation
{
	private static final long serialVersionUID = 1L;
	public static final String ID = "UNUSED_MYCONTENT";

	private final Map<String, String> metadataHtml = new HashMap<String, String>();

	public UnusedContentCleanup()
	{
		super(UnusedContentCleanupOperationFactory.class, "createWithContent");
	}

	@Override
	protected Object[] getArgs()
	{
		return new Object[]{metadataHtml.values()};
	}

	@Override
	public WorkflowOperation createPostSaveWorkflowOperation()
	{
		return null;
	}

	@Override
	public WorkflowOperation createPreSaveWorkflowOperation()
	{
		return get();
	}

	public void put(String key, String html)
	{
		metadataHtml.put(key, html);
	}

	@Override
	public String getName()
	{
		return null;
	}
}
