/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.operations;

import java.util.HashMap;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.core.scripting.WorkflowScriptConstants;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author jmaginnis
 */
@SecureOnCall(priv = "NEWVERSION_ITEM")
public class NewVersionOperation extends AbstractCloneOperation
{
	private static final String NEW_VERSION_SCRIPT_NAME = "newVersion"; //$NON-NLS-1$

	@AssistedInject
	protected NewVersionOperation()
	{
		this(true);
	}

	@AssistedInject
	protected NewVersionOperation(@Assisted boolean copyAttachments)
	{
		super(copyAttachments);
	}

	@Override
	protected Item initItemUuidAndVersion(Item newItem, Item oldItem)
	{
		int latestVersion = itemService.getLatestVersion(getId());
		newItem.setUuid(oldItem.getUuid());
		newItem.setVersion(latestVersion + 1);
		return newItem;
	}

	@Override
	protected void finalProcessing(Item origItem, Item item)
	{
		super.finalProcessing(origItem, item);

		setState(ItemStatus.DRAFT);
		// new version script
		PropBagEx newxml = (PropBagEx) getItemPack().getXml().clone();

		final String script = getItemdef().getWizard().getRedraftScript();
		if( !Check.isEmpty(script) )
		{
			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put(WorkflowScriptConstants.NEW_XML, newxml);
			ScriptContext context = createScriptContext(attributes);

			workflowOpService.executeScript(script, NEW_VERSION_SCRIPT_NAME, context, true);
		}
		getItemPack().setXml(newxml);
	}

	@Override
	protected void doHistory()
	{
		createHistory(HistoryEvent.Type.newversion);
	}
}
