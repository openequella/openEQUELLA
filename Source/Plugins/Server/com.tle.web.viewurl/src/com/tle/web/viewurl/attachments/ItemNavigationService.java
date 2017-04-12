package com.tle.web.viewurl.attachments;

import java.util.List;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;

/**
 * @author aholland
 */
public interface ItemNavigationService
{
	/**
	 * @param item The item to add nodes to
	 * @param existing Uses this to determine the next index to begin inserting
	 *            new navigation nodes at
	 * @param attachments The attachments you wish to build nodes for
	 * @param nodeAdded An optional call-back for each ItemNavigationNode that
	 *            is added by this method
	 * @return The last index that was inserted. Not overly useful.
	 */
	void populateTreeNavigationFromAttachments(Item item, List<ItemNavigationNode> existing,
		List<? extends IAttachment> attachments, NodeAddedCallback nodeAdded);

	interface NodeAddedCallback
	{
		void execute(int overallIndex, ItemNavigationNode node);
	}
}
