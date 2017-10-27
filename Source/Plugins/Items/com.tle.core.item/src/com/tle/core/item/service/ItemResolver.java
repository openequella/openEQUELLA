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

package com.tle.core.item.service;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.IAttachment;

/**
 * Based on an item ID and an extension type, load the relevant item.
 * <p>
 * E.g.
 * 
 * <pre>
 * <code>
 * CloudItem cloudItem = itemResolver.getItem(itemId, "cloud");
 * Item standardItem = itemResolver.getItem(itemId, null);
 * </code>
 * </pre>
 * 
 * @author Aaron
 */
@NonNullByDefault
public interface ItemResolver
{
	@Nullable
	<I extends IItem<?>> I getItem(ItemKey itemKey, @Nullable String extensionType);

	PropBagEx getXml(IItem<?> item, @Nullable String extensionType);

	IAttachment getAttachmentForUuid(ItemKey itemKey, String attachmentUuid, @Nullable String extensionType);

	int getLiveItemVersion(String uuid, @Nullable String extensionType);

	/**
	 * 
	 * @param item
	 * @param attachment
	 * @return true if the attachment is restricted AND the user doesn't have permissions to view it.
	 */
	boolean checkRestrictedAttachment(IItem<?> item, IAttachment attachment, @Nullable String extensionType);

	/**
	 * 
	 * @param item
	 * @param extensionType
	 * @return true if the user can view any restricted attachment on this item
	 */
	boolean canViewRestrictedAttachments(IItem<?> item, @Nullable String extensionType);

	/**
	 * 
	 * @param item
	 * @param attachment
	 * @return true if the user can mark any attachment as restricted on the item. 
	 */
	boolean canRestrictAttachments(IItem<?> item, @Nullable String extensionType);
}
