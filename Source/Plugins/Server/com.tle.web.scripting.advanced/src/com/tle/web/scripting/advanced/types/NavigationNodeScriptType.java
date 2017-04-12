package com.tle.web.scripting.advanced.types;

import java.io.Serializable;
import java.util.List;

/**
 * NavigationNode in script
 */
public interface NavigationNodeScriptType extends Serializable
{
	/**
	 * @return The display name of the node
	 */
	String getDescription();

	/**
	 * @param description The display name of the node
	 */
	void setDescription(String description);

	/**
	 * @return An unmodifiable list. Use addTab(String, AttachmentScriptType,
	 *         NavigationNodeScriptType) if you want to add a tab to this list.
	 */
	List<NavigationTabScriptType> getTabs();
}
