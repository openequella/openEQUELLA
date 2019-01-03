/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.viewable;

import java.net.URI;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.Nullable;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.web.sections.Bookmark;

public interface ViewableItem<I extends IItem<?>>
{
	IAttachment getAttachmentByUuid(String uuid);

	IAttachment getAttachmentByFilepath(String filepath);

	I getItem();

	PropBagEx getItemxml();

	FileHandle getFileHandle();

	@Nullable
	WorkflowStatus getWorkflowStatus();

	String getItemdir();

	URI getServletPath();

	ItemKey getItemId();

	/**
	 * Indicates whether the item is "real", ie, if it has been saved to the
	 * database, etc... For real? Aiiiiii.
	 */
	boolean isItemForReal();

	/**
	 * Update this viewable item with a modified item (ie the details of the
	 * viewable item may have changed)
	 * 
	 * @param pack
	 * @param status
	 */
	void update(ItemPack<I> pack, WorkflowStatus status);

	/**
	 * Seems to be much like update, but forces an internal invalidation of the
	 * known item status.
	 */
	void refresh();

	Set<String> getPrivileges();

	boolean isDRMApplicable();

	Bookmark createStableResourceUrl(String path);

	/**
	 * Sets the flag to say that this viewable item was the one requested in the
	 * url.
	 * 
	 * @param requested
	 */
	void setFromRequest(boolean requested);

	boolean isFromRequest();

	String getItemExtensionType();
}
