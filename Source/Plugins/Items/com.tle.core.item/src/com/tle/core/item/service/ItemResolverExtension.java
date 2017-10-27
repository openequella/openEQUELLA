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
 * @author Aaron
 */
@NonNullByDefault
public interface ItemResolverExtension
{
	@Nullable
	<I extends IItem<?>> I resolveItem(ItemKey itemKey);

	@Nullable
	PropBagEx resolveXml(IItem<?> item);

	@Nullable
	IAttachment resolveAttachment(ItemKey itemKey, String attachmentUuid);

	int getLiveItemVersion(String uuid);

	boolean checkRestrictedAttachment(IItem<?> item, IAttachment attachment);

	boolean canViewRestrictedAttachments(IItem<?> item);

	boolean canRestrictAttachments(IItem<?> item);
}