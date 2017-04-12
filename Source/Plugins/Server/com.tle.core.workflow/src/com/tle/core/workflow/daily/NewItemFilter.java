/*
 * Created on Nov 5, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.daily;

import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Multimap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemStatus;
import com.tle.core.notification.beans.Notification;
import com.tle.core.workflow.filters.BaseFilter;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public class NewItemFilter extends BaseFilter
{
	private final Multimap<String, String> collectionMap;
	@Inject
	private NewItemFactory factory;

	@AssistedInject
	protected NewItemFilter(@Assisted Multimap<String, String> collectionMap)
	{
		this.collectionMap = collectionMap;
	}

	@Override
	public AbstractWorkflowOperation[] createOperations()
	{
		return new AbstractWorkflowOperation[]{factory.createOperation(collectionMap),
				workflowFactory.reIndexIfRequired()};
	}

	@Override
	public FilterResults getItemIds()
	{
		if( collectionMap.keySet().isEmpty() )
		{
			Iterator<ItemIdKey> empty = Collections.emptyIterator();
			return new FilterResults(0, empty);
		}
		return super.getItemIds();
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		values.put("date", calendar.getTime());
		values.put("status", ItemStatus.LIVE.name());
		values.put("itemdefs", collectionMap.keySet());
	}

	@Override
	public String getWhereClause()
	{
		return "status = :status and moderation.liveApprovalDate > :date and itemDefinition.uuid in (:itemdefs)";
	}

	@Override
	public boolean isReadOnly()
	{
		return true;
	}

	public static class NewItemOperation extends AbstractWorkflowOperation
	{
		private Multimap<String, String> collectionMap;

		@Inject
		public NewItemOperation(@Assisted Multimap<String, String> collectionMap)
		{
			this.collectionMap = collectionMap;
		}

		@Override
		public boolean execute()
		{
			addNotifications(getItemId(), collectionMap.get(getItemdef().getUuid()), Notification.REASON_WENTLIVE2,
				true);
			return false;
		}

	}
}
