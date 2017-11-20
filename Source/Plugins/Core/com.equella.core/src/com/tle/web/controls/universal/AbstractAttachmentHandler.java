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

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.core.item.service.ItemResolver;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;

/**
 * An abstract {@link AttachmentHandler}. It contains a
 * {@link FreemarkerFactory} and {@link EventGenerator} and registers itself as
 * a sub-inner Section off the parentId passed into the
 * {@link #onRegister(SectionTree, String, UniversalControlState)} method.
 * 
 * @see AbstractDetailsAttachmentHandler
 * @author jolz
 * @param <M>
 */
@NonNullByDefault
public abstract class AbstractAttachmentHandler<M> extends AbstractPrototypeSection<M> implements AttachmentHandler
{
	@ViewFactory(fixed = false)
	protected FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;

	protected UniversalSettings settings;
	protected boolean singular;

	protected UniversalControlState dialogState;

	@Inject
	private ItemResolver itemResolver;

	@Override
	public void onRegister(SectionTree tree, String parentId, UniversalControlState state)
	{
		this.settings = new UniversalSettings(state.getControlConfiguration());
		this.dialogState = state;
		tree.registerSubInnerSection(this, parentId);
	}

	@Override
	public void setSingular(boolean singular)
	{
		this.singular = singular;
	}

	public boolean isMultipleAllowed(SectionInfo info)
	{
		return isMultiple() && !dialogState.isReplacing(info);
	}

	public boolean isMultiple()
	{
		return settings.isMultipleSelection();
	}

	public boolean isMaxFilesEnabled(SectionInfo info)
	{
		return isMaxFiles() && !dialogState.isReplacing(info);
	}

	public boolean isMaxFiles()
	{
		return settings.isMaxFilesEnabled();
	}

	public int getMaxFiles()
	{
		return settings.getMaxFiles();
	}

	public UniversalControlState getDialogState()
	{
		return dialogState;
	}

	@Override
	public boolean show()
	{
		return true;
	}

	@Override
	public boolean isHiddenFromSummary(IAttachment attachment)
	{
		final Item item = dialogState.getRepository().getItem();
		return itemResolver.checkRestrictedAttachment(item, attachment, null);
	}

	public boolean canRestrictAttachments()
	{
		final Item item = dialogState.getRepository().getItem();
		return itemResolver.canRestrictAttachments(item, null);
	}
}
