package com.tle.core.workflow.filters;

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
import com.tle.core.services.item.DrmService;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

public class DRMUpdateFilter extends BaseFilter
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
	public AbstractWorkflowOperation[] createOperations()
	{
		return new AbstractWorkflowOperation[]{opFactory.get(), workflowFactory.saveNoSaveScript(true)};
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
	public static class DrmUpdateOperation extends AbstractWorkflowOperation
	{
		@Inject
		private DrmService drmService;

		@Override
		public boolean execute()
		{
			DrmSettings drmSettings = getItem().getDrmSettings();
			List<WizardPage> pages = getItemdef().getWizard().getPages();
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
