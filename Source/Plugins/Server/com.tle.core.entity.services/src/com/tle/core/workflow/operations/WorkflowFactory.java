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

package com.tle.core.workflow.operations;

import java.util.List;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.google.inject.assistedinject.Assisted;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.BindFactory;
import com.tle.core.workflow.operations.tasks.AcceptOperation;
import com.tle.core.workflow.operations.tasks.ArchiveOperation;
import com.tle.core.workflow.operations.tasks.AssignOperation;
import com.tle.core.workflow.operations.tasks.InsecureArchiveOperation;
import com.tle.core.workflow.operations.tasks.OfflineRedraft;
import com.tle.core.workflow.operations.tasks.RedraftOperation;
import com.tle.core.workflow.operations.tasks.RejectOperation;
import com.tle.core.workflow.operations.tasks.SecureSubmitOperation;
import com.tle.core.workflow.operations.tasks.SubmitOperation;

@BindFactory
public interface WorkflowFactory
{
	StartLockOperation startLock();

	StartEditForRedraft editForRedraft();

	StartEditForMoveOperation editForMove();

	StartEditOperation startEdit(boolean modifyAttachments);

	StartEditForSaveAndContinueOperation startEditForSaveAndContinue(String stagingUuid);

	RedraftOperation redraft();

	OfflineRedraft offlineRedraft();

	SubmitOperation submit();

	SecureSubmitOperation secureSubmit(String message);

	SubmitOperation submit(String message);

	StatusOperation status();

	SaveOperation save();

	SaveBackgroundOperation saveBackground();

	SaveNoIndexingOperation saveNoIndexing(boolean noAutoArchive, String stagingID);

	SaveNoSaveScript saveNoSaveScript(boolean noSaveScript);

	SaveOperation saveUnlock(boolean unlock);

	SaveOperation saveWithOperations(boolean unlock, @Assisted("pre") List<WorkflowOperation> preSave,
		@Assisted("post") List<WorkflowOperation> postSave);

	EditMetadataOperation editMetadata(ItemPack<Item> pack);

	ReviewOperation review();

	ReviewOperation review(boolean force);

	CreateOperation create();

	CreateOperation create(ItemDefinition collection, ItemStatus status);

	CreateOperation create(ItemDefinition collection, StagingFile staging);

	CreateOperation create(ItemDefinition collection);

	CreateOperation create(PropBagEx initialXml, ItemDefinition definition, StagingFile staging);

	ArchiveOperation archive();

	InsecureArchiveOperation insecureArchive();

	AssignOperation assign(String taskId);

	MetadataMapOperation metadataMap();

	NewVersionOperation newVersion();

	NewVersionOperation newVersion(boolean attachments);

	AcceptOperation accept(@Assisted("taskId") String taskId, @Assisted("comment") String comment);

	RejectOperation reject(@Assisted("taskId") String taskId, @Assisted("comment") String comment,
		@Assisted("step") String step);

	CommentOperation comment(@Assisted("taskId") String taskId, @Assisted("comment") String comment);

	ReindexOnlyOperation reindexOnly(@Assisted("wait") boolean wait);

	DeleteOperation delete();

	PurgeOperation purge(@Assisted("wait") boolean wait);

	ReactivateOperation reactivate();

	ResetOperation reset();

	RestoreDeletedOperation restore();

	SuspendOperation suspend();

	CancelEditOperation cancelEdit(@Assisted("stagingId") @Nullable String stagingId, @Assisted("unlock") boolean unlock);

	ReassignOwnershipOperation changeOwner(@Assisted("toOwner") String toOwner);

	UserDeletedOperation userDeleted(String userID);

	ModifyCollaboratorsOperation modifyCollaborators(String userId, boolean remove);

	ChangeUserIdOperation changeUserId(@Assisted("fromUserId") String fromUserId, @Assisted("toUserId") String toUserId);

	ModifyCollaboratorsOperation addCollaborators(Set<String> allCollabs);

	ModifyNotificationsOperation modifyNotifications(Set<String> userIds);

	ClearNotificationOperation clearNotification(long notificationId);

	ReIndexIfRequiredOperation reIndexIfRequired();

	ForceModificationOperation forceModify();

	EditExistingItemMetadataOperation editExistingItemMetadata();

	ResumeOperation resume();

	UpdateReferencedURLsOperation updateReferencedUrls();

	NotifyBadUrlOperation notifyBadUrl();

	SetItemThumbnailOperation setItemThumbnail(String thumb);
}
