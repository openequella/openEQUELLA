package com.tle.core.freetext.reindex;

import com.tle.common.security.PrivilegeTree.Node;

public interface ReindexHandler
{
	/**
	 * Allow the handler to specify the reindex filter that should be used for a
	 * target if privileges are changed. For example, a change to the
	 * DISCOVER_ITEM privilege at the collection level would require all items
	 * in that collection to be reindexed, so an ItemdefFilter object would be
	 * returned.
	 * 
	 * @param target the object with the privileges being modified.
	 * @return the reindex filter to use, or <code>null</code> to prevent any
	 *         reindexing.
	 */
	ReindexFilter getReindexFilter(Node node, Object domainObject);

}
