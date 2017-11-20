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

package com.tle.web.search.actions;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.dialog.model.DialogModel;

@NonNullByDefault
public abstract class AbstractShareSearchQueryDialog extends EquellaDialog<DialogModel>
{
	@PlugKey("actions.share.dialog.title")
	protected static Label TITLE_LABEL;

	private JSCallable reloadParent;

	protected abstract AbstractShareSearchQuerySection getContentSection();

	protected AbstractShareSearchQueryDialog()
	{
		setAjax(true);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		getContentSection().setContainerDialog(this);
		super.registered(id, tree);
		tree.registerInnerSection(getContentSection(), id);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		reloadParent = addParentCallable(new ReloadFunction(false));
		super.treeFinished(id, tree);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return SectionUtils.renderSection(context, getContentSection());
	}

	public void close(SectionInfo info)
	{
		closeDialog(info, reloadParent);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE_LABEL;
	}

	@Override
	public DialogModel instantiateDialogModel(SectionInfo info)
	{
		return new DialogModel();
	}
}
