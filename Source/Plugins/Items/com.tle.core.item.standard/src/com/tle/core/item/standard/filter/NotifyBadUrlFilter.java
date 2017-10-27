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

package com.tle.core.item.standard.filter;

import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.ReferencedURL;
import com.tle.core.item.operations.WorkflowOperation;

@SuppressWarnings("nls")
public class NotifyBadUrlFilter extends AbstractStandardOperationFilter
{
	private ReferencedURL rurl;

	@AssistedInject
	public NotifyBadUrlFilter(@Assisted ReferencedURL rurl)
	{
		this.rurl = rurl;
	}

	@Override
	protected WorkflowOperation[] createOperations()
	{
		return new WorkflowOperation[]{operationFactory.notifyBadUrl(), operationFactory.reIndexIfRequired(),};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("rurl", rurl);
	}

	@Override
	public String getWhereClause()
	{
		return "i.status IN ('LIVE', 'REVIEW', 'MODERATING', 'ARCHIVED') AND :rurl IN ELEMENTS(i.referencedUrls)";
	}

}
