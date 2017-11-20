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

package com.tle.web.scripting.advanced.objects;

import java.util.List;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.web.scripting.advanced.types.NavigationNodeScriptType;
import com.tle.web.scripting.advanced.types.NavigationTabScriptType;

/**
 * Referenced by the 'nav' variable in script. The item navigation tree is what
 * is shown when viewing the item with the navigation viewer (like IMS
 * packages). Tabs are what ultimately display attachments, attachments cannot
 * be added directly to navigation nodes.
 * 
 * @author aholland
 */
public interface NavigationScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "nav"; //$NON-NLS-1$

	/**
	 * Retrieves an existing node based on it's name/description Does an exact,
	 * case sensitive match
	 * 
	 * @param description The display name of the node to find
	 * @return The NavigationNodeScriptType with a matching description, or null
	 *         if not found
	 */
	NavigationNodeScriptType getNodeByDescription(String description);

	/**
	 * Add a navigation node to the item navigation tree.
	 * 
	 * @param description A display name for the node
	 * @param parentNode Specify null for the root level
	 * @return The new node that was added
	 */
	NavigationNodeScriptType addNode(String description, NavigationNodeScriptType parentNode);

	/**
	 * Remove a node from the item navigation tree.
	 * 
	 * @param node The node to remove. If the node is not found in the tree the
	 *            method will continue as normal.
	 */
	void removeNode(NavigationNodeScriptType node);

	/**
	 * Add a tab to a navigation node. The tab will be added to the end of the
	 * list of current tabs (if any) If only one tab is added to a node, the tab
	 * <b>bar</b> is not displayed.
	 * 
	 * @param description A display name for the tab. Not shown if there is only
	 *            one tab present.
	 * @param node The node to add the tab to.
	 * @param attachment
	 * @return The new tab that was added
	 */
	NavigationTabScriptType addTab(String description, AttachmentScriptType attachment, NavigationNodeScriptType node);

	/**
	 * Create navigation nodes based on the current attachments. Does the same
	 * as the 'initialise' button does on the navigation builder control.
	 */
	void autocreate();

	/**
	 * @param allowSplit 'Split view' enabled in the tree viewer.
	 */
	void setAllowSplitOption(boolean allowSplit);

	/**
	 * @return 'Split view' enabled in the tree viewer.
	 */
	boolean isAllowSplitOption();

	/**
	 * Clears the entire navigation tree. A slightly more efficient method of
	 * deleting all root nodes than calling removeChildNodes(null)
	 */
	void removeAll();

	/**
	 * Returns a list of child nodes of the given parentNode.
	 * 
	 * @param parentNode The node to get the children of. If null, all
	 *            root-level nodes are returned.
	 * @return All children of parentNode
	 */
	List<NavigationNodeScriptType> listChildNodes(NavigationNodeScriptType parentNode);

	/**
	 * Removes all child nodes of the given parentNode.
	 * 
	 * @param parentNode The node to remove the children from. If null, all
	 *            root-level nodes are removed.
	 * @returns The number of nodes that were removed.
	 */
	int removeChildNodes(NavigationNodeScriptType parentNode);
}
