package com.tle.core.item.standard;

import java.util.Collection;

import com.google.common.collect.Multimap;
import com.google.inject.assistedinject.Assisted;
import com.tle.beans.ReferencedURL;
import com.tle.beans.item.ItemKey;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.standard.filter.ChangeUserIdFilter;
import com.tle.core.item.standard.filter.DRMUpdateFilter;
import com.tle.core.item.standard.filter.NewItemFilter;
import com.tle.core.item.standard.filter.NotifyBadUrlFilter;
import com.tle.core.item.standard.filter.RefreshCollectionItemDataFilter;
import com.tle.core.item.standard.filter.RefreshSchemaItemDataFilter;
import com.tle.core.item.standard.filter.RemoveDeletedItemsFilter;
import com.tle.core.item.standard.filter.UserDeletedFilter;
import com.tle.core.item.standard.filter.workflow.ArchiveOldFilter;
import com.tle.core.item.standard.filter.workflow.WorkflowChangeFilter;

@BindFactory
public interface FilterFactory
{
	UserDeletedFilter userDeleted(String user);

	ChangeUserIdFilter changeUserId(@Assisted("fromUserId") String fromUserId, @Assisted("toUserId") String toUserId);

	ArchiveOldFilter archiveOld(ItemKey itemKey);

	RefreshCollectionItemDataFilter refreshCollectionItems(long collectionId);

	RefreshSchemaItemDataFilter refreshSchemaItems(long schemaId);

	DRMUpdateFilter drmUpdate(long collectionId, Collection<String> pageIds);

	RemoveDeletedItemsFilter removeDeleted(int daysOld);

	NotifyBadUrlFilter notifyBadUrl(ReferencedURL rurl);

	NewItemFilter createFilter(Multimap<String, String> collectionMap);

	/*
	 * Workflow
	 */

	WorkflowChangeFilter workflowChanged(long collectionId);

}
