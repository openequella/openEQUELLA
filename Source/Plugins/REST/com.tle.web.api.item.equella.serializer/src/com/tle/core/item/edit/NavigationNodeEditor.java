package com.tle.core.item.edit;

import java.util.List;

public interface NavigationNodeEditor
{
	String getUuid();

	void editName(String name);

	void editIcon(String icon);

	void editImsId(String imsId);

	/**
	 * Edit a tab. Attachment must already exist before calling this.
	 * 
	 * @param index
	 * @param name
	 * @param attachmentUuid
	 * @param viewer
	 */
	void editTab(int index, String name, String attachmentUuid, String viewer);

	/**
	 * Change the number of tabs. At least the given number of tabs must already
	 * exist.
	 * 
	 * @param numTabs
	 */
	void editTabCount(int numTabs);

	/**
	 * Edit the child list. All children must already exist.
	 * 
	 * @param childUuids
	 */
	void editChildrenList(List<String> childUuids);
}
