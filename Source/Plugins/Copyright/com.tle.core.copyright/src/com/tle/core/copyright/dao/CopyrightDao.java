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

package com.tle.core.copyright.dao;

import java.util.List;
import java.util.Map;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;

public interface CopyrightDao<H extends Holding, P extends Portion, S extends Section>
{
	void deleteAllForItem(Item item);

	long save(Object entity);

	H getHoldingForItem(Item item);

	List<Item> getAllItemsForHolding(H holding);

	S getSectionForAttachment(Item item, String attachmentUuid);

	long saveHolding(Item item, H holding);

	void savePortions(Item item, H holding, List<P> portions);

	void updateHoldingReference(H holding, List<Item> portionItems);

	Map<Long, H> getHoldingsForItems(List<Item> items);

	List<P> getPortionsForItems(List<Item> items);

	H getHoldingInItem(Item item);

	Attachment getSectionAttachmentForFilepath(Item item, String filepath);
}
