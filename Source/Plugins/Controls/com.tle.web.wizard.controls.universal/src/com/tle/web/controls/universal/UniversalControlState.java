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

package com.tle.web.controls.universal;

import java.util.Collection;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.wizard.impl.WebRepository;

@NonNullByDefault
public interface UniversalControlState
{
	/**
	 * Get a {@link ViewableItem} for the currently editing wizard.
	 * 
	 * @param info
	 * @return
	 */
	ViewableItem getViewableItem(SectionInfo info);

	/**
	 * Get the {@link WebRepository}.
	 * 
	 * @return
	 */
	WebRepository getRepository();

	/**
	 * Get the control configuration which contains information about each
	 * handler type.
	 * 
	 * @return The configuration object
	 */
	CustomControl getControlConfiguration();

	/**
	 * Set an attribute for this dialog. Each dialog opening has a new set of
	 * attributes.
	 * 
	 * @param info
	 * @param key
	 * @param value
	 */
	void setAttribute(SectionInfo info, Object key, Object value);

	/**
	 * Get an attribute for this dialog.
	 * 
	 * @see UniversalControlState#setAttribute(SectionInfo, Object, Object)
	 * @param info
	 * @param key
	 * @return
	 */
	@Nullable
	<T> T getAttribute(SectionInfo info, Object key);

	/**
	 * Determine if the dialog was opened to edit an existing attachment.
	 * 
	 * @param info
	 * @return true if it is editing
	 */
	boolean isEditing(SectionInfo info);

	/**
	 * Determine if the dialog was opened to replace an existing attachment
	 * (re-use it's uuid).
	 * 
	 * @param info
	 * @return true if it's replacing an existing attachment's uuid.
	 */
	boolean isReplacing(SectionInfo info);

	/**
	 * Add an {@link Attachment} to the item's global attachment list.
	 * 
	 * @param info
	 * @param attachment
	 */
	void addAttachment(SectionInfo info, Attachment attachment);

	/**
	 * Add an attachment uuid to the item xml at the control's xpath.
	 * 
	 * @param info
	 * @param uuid
	 */
	void addMetadataUuid(SectionInfo info, String uuid);

	/**
	 * Remove an {@link Attachment} from the item's global attachment list and
	 * ensure it is removed from the navigation tree.
	 * 
	 * @param info
	 * @param attachment
	 */
	void removeAttachment(SectionInfo info, Attachment attachment);

	/**
	 * Remove a collection of Attachments from the item's global attachment list
	 * and ensure they are removed from the navigation tree.
	 * 
	 * @param info
	 * @param attachment
	 */
	void removeAttachments(SectionInfo info, Collection<Attachment> attachments);

	/**
	 * Remove an element from the item xml at the control's xpath matching the
	 * given uuid.
	 * 
	 * @param info
	 * @param uuid
	 */
	void removeMetadataUuid(SectionInfo info, String uuid);

	/**
	 * Perform a save. Does exactly the same thing as clicking the dialog's save
	 * button.
	 * 
	 * @param info
	 */
	void save(SectionInfo info);

	/**
	 * Cancel the dialog. Does exactly the same thing as the close button of the
	 * dialog.
	 * 
	 * @param info
	 */
	void cancel(SectionInfo info);

	/**
	 * Gets all the {@link Attachment} objects pointed to by the uuids at the
	 * control's xpath.
	 * 
	 * @return
	 */
	Collection<Attachment> getAttachments();

	/**
	 * Get the the {@link Attachment} that will be replaced. Only valid if
	 * {@link #isReplacing(SectionInfo)} returns true.
	 * 
	 * @param info
	 * @return
	 */
	@Nullable
	Attachment getReplacedAttachment(SectionInfo info);

	UniversalResourcesDialog getDialog();
}
