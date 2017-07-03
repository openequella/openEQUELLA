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

package com.tle.web.remotecaching;

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class RemoteCachingAddGroupDialog
	extends
		AbstractOkayableDialog<RemoteCachingAddGroupDialog.RemoteCachingAddGroupDialogModel>
{
	@PlugKey("addgrouping.dialog.title")
	private static Label LABEL_TITLE;

	@PlugKey("addgrouping.blank.message")
	private static Label LABEL_NOT_BLANK;

	@Component(stateful = false)
	private TextField groupingName;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "addgroupingdialog";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setAjax(true);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("addgroupingdialog.ftl", this);
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		return new OverrideHandler(jscall(getOkCallback(), groupingName.createGetExpression()),
			jscall(getCloseFunction())).addValidator(groupingName.createNotBlankValidator().setFailureStatements(
			Js.alert_s(LABEL_NOT_BLANK)));
	}

	public TextField getGroupingName()
	{
		return groupingName;
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	public RemoteCachingAddGroupDialogModel instantiateDialogModel(SectionInfo info)
	{
		return new RemoteCachingAddGroupDialogModel();
	}

	public static class RemoteCachingAddGroupDialogModel extends DialogModel
	{
		// Nothing to declare
	}
}
