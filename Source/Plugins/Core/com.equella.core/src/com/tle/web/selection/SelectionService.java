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

package com.tle.web.selection;

import java.util.List;
import java.util.Set;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewableResource;

@NonNullByDefault
public interface SelectionService
{
	@Nullable
	SelectionSession getCurrentSession(SectionInfo info);

	/**
	 * @param info
	 * @return DOWNLOAD_ITEM if in an EQUELLA repo selection session, otherwise
	 *         DISCOVER_ITEM
	 */
	String getSearchPrivilege(SectionInfo info);

	void disableSelection(SectionInfo info);

	void forwardToSelectable(SectionInfo info, SelectableInterface selectable);

	void forwardToCheckout(SectionInfo info);

	void returnFromSession(SectionInfo info);

	List<BaseEntityLabel> filterEntities(List<BaseEntityLabel> labels, Long[] entityIds);

	List<? extends BaseEntity> filterFullEntities(List<? extends BaseEntity> entities, Set<String> entityIds);

	SectionInfo getSelectionSessionForward(SectionInfo info, SelectionSession session, SelectableInterface selection);

	void setupSelectionSession(SectionInfo info, SelectionSession session);

	void forwardToNewSession(SectionInfo info, SelectionSession session, SelectableInterface selectable);

	SelectableInterface getNamedSelectable(String selectable);

	void addSelectedPath(SectionInfo info, ItemKey itemId, String resource, String title, String description,
		@Nullable TargetFolder folder, @Nullable String extensionType);

	void addSelectedPath(SectionInfo info, IItem<?> item, String resource, @Nullable TargetFolder folder,
		@Nullable String extensionType);

	void addSelectedItem(SectionInfo info, IItem<?> item, @Nullable TargetFolder folder,
		@Nullable String extensionType);

	void addSelectedResource(SectionInfo info, SelectedResource selection, boolean forward);

	JSCallable getSelectFunction(SectionInfo info, String functionName, SelectedResource sampleResource);

	List<SelectionHistory> getRecentSelections(SelectionSession session, final int maximum);

	List<SelectionHistory> getRecentSelections(SectionInfo info, final int maximum);

	SelectedResource createItemSelection(SectionInfo info, IItem<?> item, @Nullable String extensionType);

	SelectedResource createItemSelection(SectionInfo info, IItem<?> item, @Nullable TargetFolder folder,
		@Nullable String extensionType);

	SelectedResource createAttachmentSelection(SectionInfo info, ItemKey itemId, IAttachment attachment,
		@Nullable TargetFolder folder, @Nullable String extensionType);

	ViewableResource createViewableResource(SectionInfo info, SelectedResource resource);

	void removeSelectedResource(SectionInfo info, SelectedResourceKey key);

	boolean canSelectItem(SectionInfo info, ViewableItem<?> vitem);

	/**
	 * @param info
	 * @param vitem
	 * @param attachmentUuid Leave null to specify generic attachment selection
	 * @return
	 */
	boolean canSelectAttachment(SectionInfo info, ViewableItem<?> vitem, @Nullable String attachmentUuid);

	boolean isSelected(SectionInfo info, ItemKey itemId, String attachmentUuid, @Nullable String extensionType,
		boolean anywhere);

	/**
	 * @param info
	 * @param folderId If null then retrieve the root
	 * @return
	 */
	TargetFolder findTargetFolder(SectionInfo info, @Nullable String folderId);

	@Nullable
	JSCallable getSelectItemFunction(SectionInfo info, ViewableItem<?> vitem);

	@Nullable
	JSCallable getSelectAttachmentFunction(SectionInfo info, ViewableItem<?> vitem);

	@Nullable
	JSCallable getSelectPackageFunction(SectionInfo info, ViewableItem<?> vitem);

	/**
	 * Much the same as the getSelectAttachmentFunction, only it's something
	 * invokable in Java
	 */
	@Nullable
	SelectAttachmentHandler getSelectAttachmentHandler(SectionInfo info, ViewableItem<?> vitem,
		@Nullable String attachmentUuid);

	@Nullable
	JSCallable getSelectAllAttachmentsFunction(SectionInfo info, ViewableItem<?> vitem);

	@Nullable
	JSCallable getSelectAttachmentFunction(SectionInfo info, ViewableItem<?> vitem, @Nullable String attachmentUuid);
}
