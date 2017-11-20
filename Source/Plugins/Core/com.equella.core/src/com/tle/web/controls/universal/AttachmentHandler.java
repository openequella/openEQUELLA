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

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public interface AttachmentHandler
{
	/**
	 * Called during the Dialog's registered() method. The handler should
	 * register any sections required and store the
	 * {@link UnivseralControlState} for later usage.
	 * 
	 * @param tree
	 * @param parentId
	 * @param state
	 */
	void onRegister(SectionTree tree, String parentId, UniversalControlState state);

	/**
	 * Return an {@link AttachmentHandlerLabel} for use in the handler selection
	 * screen.
	 * 
	 * @return The label
	 */
	AttachmentHandlerLabel getLabel();

	/**
	 * Test whether or not the given attachment is supported by this handler. <br>
	 * Used for editing and removal.
	 * 
	 * @param attachment
	 * @return true is the handler can handle this attachment
	 */
	boolean supports(IAttachment attachment);

	/**
	 * Called when a new attachment for this handler is going to be created.
	 * This method should reset the UI components to their default values and
	 * clear any state stored in the {@link UniversalControlState}.
	 * 
	 * @param info
	 */
	void createNew(SectionInfo info);

	/**
	 * Prepare the handler for editing the given attachment. Should setup the UI
	 * components and state attributes with values from the attachment. It
	 * should <b>NOT</b> keep a reference to the given attachment or modify it
	 * any way.
	 * 
	 * @param info
	 * @param attachment
	 */
	void loadForEdit(SectionInfo info, Attachment attachment);

	/**
	 * Render the contents of the dialog when adding/editing an attachment with
	 * this handler. <br>
	 * The {@link DialogRenderOptions} object can be used to automatically add a
	 * save/(add/replace) button which will call {@link #validate(SectionInfo)}
	 * and {@link #saveChanges(SectionInfo, String)} on your handler.
	 * 
	 * @param context
	 * @param renderOptions
	 * @return A renderable for the contents of the dialog.
	 */
	SectionRenderable render(RenderContext context, DialogRenderOptions renderOptions);

	/**
	 * Validate the UI of your handler before saving any changes. <br>
	 * If validation succeeds and you return true, either
	 * {@link #saveEdited(SectionInfo, Attachment)} or
	 * {@link #saveChanges(SectionInfo, String)} will be called depending on if
	 * your addding/replacing or editing.
	 * 
	 * @param info
	 * @return true if ready to save changes
	 */
	boolean validate(SectionInfo info);

	/**
	 * Commit changes to the attachment being edited. <br>
	 * The given attachment should be modified in place, no need to modify the
	 * itemxml or add a new attachment for it.
	 * 
	 * @param info
	 * @param attachment
	 */
	void saveEdited(SectionInfo info, Attachment attachment);

	/**
	 * Actually commit the changes to the item. <br>
	 * This method should add any new attachments to the item and their UUID's
	 * to the itemxml. It should also move any temporary files to their final
	 * resting place within the item's staging area. If given a non-null
	 * replacementUuid the first attachment created should have it's UUID set to
	 * the given one and there is no need to add the UUID to the item xml.
	 * 
	 * @param info
	 * @param replacementUuid The UUID to be used if replacing an existing
	 *            attachment.
	 */
	void saveChanges(SectionInfo info, String replacementUuid);

	/**
	 * Cleanup any temporary resources used by the handler. Normally you only
	 * want to clean up external resources such as temporary files as usually
	 * {@link #createNew(SectionInfo)} and
	 * {@link #loadForEdit(SectionInfo, Attachment)} will prepare the UI and
	 * state appropriately.
	 * 
	 * @param info
	 */
	void cancelled(SectionInfo info);

	/**
	 * Remove attachment from the item. <br>
	 * The handler also need to remove any external resources such as files or
	 * other sub attachments which are associated with the given attachment. The
	 * willBeReplaced flag determines whether you should remove the attachment's
	 * UUID from the item xml.
	 * 
	 * @param info
	 * @param attachment
	 * @param willBeReplaced If the attachment is about to be replaced with
	 *            another with the same UUID. If so, don't remove the UUID from
	 *            the item xml.
	 */
	void remove(SectionInfo info, Attachment attachment, boolean willBeReplaced);

	/**
	 * Used for a custom style on the dialog
	 * 
	 * @return
	 */
	String getHandlerId();

	/**
	 * Called when this is the only handler available and it has been
	 * autoselected. Current only used for styling the ResourceHandler
	 * differently.
	 */
	void setSingular(boolean singular);

	Label getTitleLabel(RenderContext context, boolean editing);

	/**
	 * Return true if the given attachment will be hidden from the summary page.
	 * 
	 * @param attachment
	 * @return
	 */
	boolean isHiddenFromSummary(IAttachment attachment);

	/**
	 * Called by {@link UniversalResourcesDialog} to decide if this handler
	 * should be shown in the list. True by default.
	 */
	boolean show();

}
