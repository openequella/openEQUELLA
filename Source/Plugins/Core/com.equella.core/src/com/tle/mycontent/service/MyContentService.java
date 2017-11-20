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

package com.tle.mycontent.service;

import java.io.InputStream;
import java.util.Set;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.mycontent.ContentHandler;
import com.tle.mycontent.web.section.ContributeMyContentAction;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public interface MyContentService
{
	boolean isMyContentContributionAllowed();

	ItemDefinition getMyContentItemDef();

	boolean isMyContentItem(Item item);

	/**
	 * @param info
	 * @return Are we able to return?
	 */
	boolean returnFromContribute(SectionInfo info);

	Set<String> getContentHandlerIds();

	String getContentHandlerNameKey(String handlerId);

	ContentHandler getHandlerForId(String handlerId);

	WorkflowOperation getEditOperation(MyContentFields fields, String filename, InputStream inputStream,
		String stagingUuid, boolean removeExistingAttachments, boolean useExistingAttachment);

	MyContentFields getFieldsForItem(ItemId itemId);

	void delete(ItemId itemId);

	void forwardToEditor(SectionInfo info, ItemId itemId);

	ContributeMyContentAction createActionForHandler(String handlerId);

	void forwardToContribute(SectionInfo info, String handlerId);

	void restore(ItemId itemId);

}
