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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.WizardPage;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.DrmSettings;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.DrmService;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;

public class DRMUpdateFilter extends AbstractStandardOperationFilter
{
	private final long itemDefinitionID;
	private Collection<String> pageIds;

	@Inject
	private Provider<DrmUpdateOperation> opFactory;

	@AssistedInject
	protected DRMUpdateFilter(@Assisted long collectionId, @Assisted Collection<String> pageIds)
	{
		this.itemDefinitionID = collectionId;
		this.pageIds = pageIds;
	}

	@Override
	public WorkflowOperation[] createOperations()
	{
		return new WorkflowOperation[]{opFactory.get(), operationFactory.saveNoSaveScript(true)};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("itemDefinition", itemDefinitionID);
		values.put("pages", pageIds);
	}

	@Override
	public String getWhereClause()
	{
		return "itemDefinition.id = :itemDefinition and drmSettings.drmPageUuid in (:pages)";
	}

	@Bind
	public static class DrmUpdateOperation extends AbstractStandardWorkflowOperation
	{
		@Inject
		private DrmService drmService;

		@Override
		public boolean execute()
		{
			DrmSettings drmSettings = getItem().getDrmSettings();
			List<WizardPage> pages = getCollection().getWizard().getPages();
			String id = drmSettings.getDrmPageUuid();
			for( WizardPage page : pages )
			{
				if( page instanceof DRMPage )
				{
					DRMPage drmPage = (DRMPage) page;
					if( drmPage.getUuid().equals(id) )
					{
						drmService.mergeSettings(drmSettings, drmPage);
						break;
					}
				}
			}
			return true;
		}
	}
}
