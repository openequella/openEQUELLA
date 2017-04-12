package com.tle.core.workflow.filters;

import java.util.Collection;

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.ReferencedURL;
import com.tle.beans.item.ItemKey;
import com.tle.core.guice.BindFactory;

@BindFactory
public interface FilterFactory
{
	UserDeletedFilter userDeleted(String user);

	ChangeUserIdFilter changeUserId(@Assisted("fromUserId") String fromUserId, @Assisted("toUserId") String toUserId);

	ArchiveOldFilter archiveOld(ItemKey itemKey);

	RefreshCollectionItemDataFilter refreshCollectionItems(long collectionId);

	RefreshSchemaItemDataFilter refreshSchemaItems(long schemaId);

	WorkflowChangeFilter workflowChanged(long collectionId);

	CheckModerationForStepsFilter checkForSteps(Collection<Long> nodeIds, boolean forceModify);

	DRMUpdateFilter drmUpdate(long collectionId, Collection<String> pageIds);

	RemoveDeletedItemsFilter removeDeleted(int daysOld);

	NotifyBadUrlFilter notifyBadUrl(ReferencedURL rurl);
}
