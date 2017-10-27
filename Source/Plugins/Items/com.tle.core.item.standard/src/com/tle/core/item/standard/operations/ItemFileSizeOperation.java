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

package com.tle.core.item.standard.operations;

import javax.inject.Inject;

import com.tle.core.quota.service.QuotaService;

public class ItemFileSizeOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	private QuotaService quotaService;

	@Override
	public boolean execute()
	{
		long file = quotaService.getFileSize(itemFileService.getItemFile(getItem()));
		if( file != getItem().getTotalFileSize() )
		{
			getItem().setTotalFileSize(file);
			return true;
		}
		return false;
	}
}
