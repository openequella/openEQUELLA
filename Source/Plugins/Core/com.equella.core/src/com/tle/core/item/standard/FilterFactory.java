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
