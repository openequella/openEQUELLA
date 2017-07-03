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