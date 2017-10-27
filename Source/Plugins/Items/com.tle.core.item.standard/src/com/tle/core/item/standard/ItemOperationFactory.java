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

import java.util.List;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.Multimap;
import com.google.inject.assistedinject.Assisted;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.operations.CancelEditOperation;
import com.tle.core.item.standard.operations.ChangeUserIdOperation;
import com.tle.core.item.standard.operations.ClearNotificationOperation;
import com.tle.core.item.standard.operations.CreateOperation;
import com.tle.core.item.standard.operations.DeleteOperation;
import com.tle.core.item.standard.operations.EditExistingItemMetadataOperation;
import com.tle.core.item.standard.operations.EditMetadataOperation;
import com.tle.core.item.standard.operations.ForceModificationOperation;
import com.tle.core.item.standard.operations.MetadataMapOperation;
import com.tle.core.item.standard.operations.ModifyCollaboratorsOperation;
import com.tle.core.item.standard.operations.ModifyNotificationsOperation;
import com.tle.core.item.standard.operations.NewItemOperation;
import com.tle.core.item.standard.operations.NewVersionOperation;
import com.tle.core.item.standard.operations.NotifyBadUrlOperation;
import com.tle.core.item.standard.operations.PurgeOperation;
import com.tle.core.item.standard.operations.ReIndexIfRequiredOperation;
import com.tle.core.item.standard.operations.ReactivateOperation;
import com.tle.core.item.standard.operations.ReassignOwnershipOperation;
import com.tle.core.item.standard.operations.ReindexOnlyOperation;
import com.tle.core.item.standard.operations.RestoreDeletedOperation;
import com.tle.core.item.standard.operations.ResumeOperation;
import com.tle.core.item.standard.operations.SaveBackgroundOperation;
import com.tle.core.item.standard.operations.SaveNoIndexingOperation;
import com.tle.core.item.standard.operations.SaveNoSaveScript;
import com.tle.core.item.standard.operations.SaveOperation;
import com.tle.core.item.standard.operations.SetItemThumbnailOperation;
import com.tle.core.item.standard.operations.StartEditForMoveOperation;
import com.tle.core.item.standard.operations.StartEditForRedraft;
import com.tle.core.item.standard.operations.StartEditForSaveAndContinueOperation;
import com.tle.core.item.standard.operations.StartEditOperation;
import com.tle.core.item.standard.operations.StartLockOperation;
import com.tle.core.item.standard.operations.UpdateReferencedURLsOperation;
import com.tle.core.item.standard.operations.UserDeletedOperation;
import com.tle.core.item.standard.operations.workflow.AcceptOperation;
import com.tle.core.item.standard.operations.workflow.ArchiveOperation;
import com.tle.core.item.standard.operations.workflow.AssignOperation;
import com.tle.core.item.standard.operations.workflow.CheckStepOperation;
import com.tle.core.item.standard.operations.workflow.InsecureArchiveOperation;
import com.tle.core.item.standard.operations.workflow.OfflineRedraft;
import com.tle.core.item.standard.operations.workflow.RedraftOperation;
import com.tle.core.item.standard.operations.workflow.RejectOperation;
import com.tle.core.item.standard.operations.workflow.ResetOperation;
import com.tle.core.item.standard.operations.workflow.ReviewOperation;
import com.tle.core.item.standard.operations.workflow.SecureSubmitOperation;
import com.tle.core.item.standard.operations.workflow.StatusOperation;
import com.tle.core.item.standard.operations.workflow.SubmitOperation;
import com.tle.core.item.standard.operations.workflow.SuspendOperation;
import com.tle.core.item.standard.operations.workflow.WorkflowCommentOperation;

@BindFactory
public interface ItemOperationFactory
{
	StartLockOperation startLock();

	StartEditForRedraft editForRedraft();

	StartEditForMoveOperation editForMove();

	StartEditOperation startEdit(boolean modifyAttachments);

	StartEditForSaveAndContinueOperation startEditForSaveAndContinue(String stagingUuid);

	SaveOperation save();

	SaveBackgroundOperation saveBackground();

	SaveNoIndexingOperation saveNoIndexing(boolean noAutoArchive, String stagingID);

	SaveNoSaveScript saveNoSaveScript(boolean noSaveScript);

	SaveOperation saveUnlock(boolean unlock);

	SaveOperation saveWithOperations(boolean unlock, @Assisted("pre") List<WorkflowOperation> preSave,
		@Assisted("post") List<WorkflowOperation> postSave);

	EditMetadataOperation editMetadata(ItemPack<Item> pack);

	CreateOperation create();

	CreateOperation create(ItemDefinition collection, ItemStatus status);

	CreateOperation create(ItemDefinition collection, StagingFile staging);

	CreateOperation create(ItemDefinition collection);

	CreateOperation create(PropBagEx initialXml, ItemDefinition definition, StagingFile staging);

	MetadataMapOperation metadataMap();

	NewVersionOperation newVersion();

	NewVersionOperation newVersion(boolean attachments);

	ReindexOnlyOperation reindexOnly(@Assisted("wait") boolean wait);

	DeleteOperation delete();

	PurgeOperation purge(@Assisted("wait") boolean wait);

	ReactivateOperation reactivate();

	RestoreDeletedOperation restore();

	SuspendOperation suspend();

	CancelEditOperation cancelEdit(@Assisted("stagingId") @Nullable String stagingId,
		@Assisted("unlock") boolean unlock);

	ReassignOwnershipOperation changeOwner(@Assisted("toOwner") String toOwner);

	UserDeletedOperation userDeleted(String userID);

	ModifyCollaboratorsOperation modifyCollaborators(String userId, boolean remove);

	ChangeUserIdOperation changeUserId(@Assisted("fromUserId") String fromUserId,
		@Assisted("toUserId") String toUserId);

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

	NewItemOperation createOperation(Multimap<String, String> collectionMap);

	/*
	 * Workflow
	 */

	AcceptOperation accept(@Assisted("taskId") String taskId, @Assisted("comment") String comment,
		@Assisted("messageUuid") String messageUuid);

	RejectOperation reject(@Assisted("taskId") String taskId, @Assisted("comment") String comment,
		@Assisted("step") String step, @Assisted("messageUuid") String messageUuid);

	WorkflowCommentOperation comment(@Assisted("taskId") String taskId, @Assisted("comment") String comment,
		@Assisted("messageUuid") String messageUuid);

	AssignOperation assign(String taskId);

	ResetOperation reset();

	ReviewOperation review();

	ReviewOperation review(boolean force);

	RedraftOperation redraft();

	OfflineRedraft offlineRedraft();

	SubmitOperation submit();

	SecureSubmitOperation secureSubmit(String message);

	SubmitOperation submit(String message);

	ArchiveOperation archive();

	InsecureArchiveOperation insecureArchive();

	StatusOperation status();

	CheckStepOperation checkSteps();
}
