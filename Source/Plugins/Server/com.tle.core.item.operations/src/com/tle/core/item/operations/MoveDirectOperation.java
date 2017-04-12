package com.tle.core.item.operations;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.common.workflow.Workflow;
import com.tle.core.schema.SchemaService;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.util.ItemHelper;
import com.tle.core.util.ItemHelper.ItemHelperSettings;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.operations.MetadataTransformingOperation;
import com.tle.core.workflow.operations.WorkflowFactory;

/**
 * @author aholland
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureOnCall(priv = SecurityConstants.MOVE_ITEM)
@SecureItemStatus(value = {ItemStatus.PERSONAL}, not = true)
public final class MoveDirectOperation extends AbstractWorkflowOperation implements MetadataTransformingOperation // NOSONAR
{
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private SchemaService schemaService;
	@Inject
	private WorkflowFactory workflowFactory;

	private final String newItemdefUuid;
	private String transform;
	private boolean dontReset;

	@AssistedInject
	private MoveDirectOperation(@Assisted final String newItemdefUuid)
	{
		this.newItemdefUuid = newItemdefUuid;
	}

	@Override
	public void setTransform(final String transform)
	{
		this.transform = transform;
	}

	@Override
	public boolean execute()
	{
		final Item item = getItem();
		final ItemDefinition newItemdef = itemdefService.getByUuid(newItemdefUuid);

		if( !dontReset && item.isModerating() )
		{
			Workflow oldWorkflow = item.getItemDefinition().getWorkflow();
			Workflow newWorkflow = newItemdef.getWorkflow();
			if( workflowId(oldWorkflow) != workflowId(newWorkflow) )
			{
				params.addOperation(workflowFactory.reset());
			}
		}
		// use the transform, if any
		if( !Check.isEmpty(transform) )
		{
			final ItemPack oldPack = new ItemPack(item, itemService.getItemXmlPropBag(item), null);
			final PropBagEx oldXml = itemHelper.convertToXml(oldPack, new ItemHelperSettings(true));

			try
			{
				final PropBagEx newXml = new PropBagEx(schemaService.transformForImport(newItemdef.getSchema().getId(),
					transform, oldXml));
				getItemPack().setXml(newXml);
			}
			catch( final Exception p )
			{
				throw new WorkflowException(
					CurrentLocale.get("com.tle.core.workflow.operations.clone.error.transforming"), p); //$NON-NLS-1$
			}
		}
		item.setItemDefinition(newItemdef);
		params.setUpdateSecurity(true);
		createHistory(HistoryEvent.Type.changeCollection);

		return true;
	}

	private long workflowId(Workflow workflow)
	{
		if( workflow == null )
		{
			return 0;
		}
		return workflow.getId();
	}

	public void setDontReset(boolean dontReset)
	{
		this.dontReset = dontReset;
	}
}
