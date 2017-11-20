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

package com.tle.web.cloud.view.actions;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.dialog.model.DialogModel;

@SuppressWarnings("nls")
@Bind
public class CloudShareWithOthersDialog extends EquellaDialog<DialogModel>
{
	@PlugKey("share.cloud.sharewithothers.dialog.title")
	public static Label DIALOG_TITLE;

	@Inject
	private CloudShareWithOthersContentSection content;

	protected JSCallable reloadParent;

	public CloudShareWithOthersDialog()
	{
		setAjax(true);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(content, id);
		reloadParent = addParentCallable(new ReloadFunction(false));
		content.getSendEmailButton().setComponentAttribute(ButtonType.class, ButtonType.SAVE);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return renderSection(context, content);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return DIALOG_TITLE;
	}

	@Override
	public DialogModel instantiateDialogModel(SectionInfo info)
	{
		return new DialogModel();
	}

	public JSCallable getReloadParent()
	{
		return reloadParent;
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		return Collections.singleton(content.getSendEmailButton());
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "shareclouddialog";
	}
}
