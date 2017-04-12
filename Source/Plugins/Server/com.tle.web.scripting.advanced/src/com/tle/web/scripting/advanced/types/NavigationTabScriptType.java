package com.tle.web.scripting.advanced.types;

import java.io.Serializable;

import com.tle.common.scripting.types.AttachmentScriptType;

/**
 * NavigationTab in script
 */
public interface NavigationTabScriptType extends Serializable
{
	/**
	 * @return The navigation node that this tab is attached to
	 */
	NavigationNodeScriptType getNode();

	/**
	 * @return Display name for the tab. Not shown if there is only one tab
	 *         present.
	 */
	String getDescription();

	/**
	 * @param description Display name for the tab. Not shown if there is only
	 *            one tab present.
	 */
	void setDescription(String description);

	/**
	 * @return The ID of the viewer used to view the attachment on this tab
	 */
	String getViewerId();

	/**
	 * @param viewerId The ID of the viewer used to view the attachment on this
	 *            tab. Use a blank string for the default viewer associated with
	 *            the attachment's MIME type.
	 */
	void setViewerId(String viewerId);

	/**
	 * @return The item attachment that this tab will display
	 */
	AttachmentScriptType getAttachment();
}
