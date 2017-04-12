package com.tle.core.services.item;

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
