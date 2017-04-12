package com.tle.web.viewable;

import java.net.URI;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.Nullable;
import com.tle.beans.filesystem.FileHandle;
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
