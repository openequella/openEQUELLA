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
