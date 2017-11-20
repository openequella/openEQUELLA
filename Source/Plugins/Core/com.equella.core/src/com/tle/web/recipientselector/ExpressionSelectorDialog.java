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

package com.tle.web.recipientselector;

import java.util.Collection;

import com.google.inject.Inject;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

/**
 * @author Peng
 */
@SuppressWarnings("nls")
@Bind
public class ExpressionSelectorDialog
	extends
		AbstractOkayableDialog<ExpressionSelectorDialog.ExpressionSelectorDialogModel>
{
	@PlugKey("selector.defaulttitle")
	private static Label LABEL_SELECTOR_TITLE;

	@PlugKey("selector.button.cancel")
	@Component
	private Button cancelGroupSelectionButton;

	@Inject
	private ExpressionSelectorSection section;

	private Label title;
	private boolean onlyUsersAndGroups;

	public ExpressionSelectorDialog()
	{
		setAjax(true);
	}

	public boolean isOnlyUsersAndGroups()
	{
		return onlyUsersAndGroups;
	}

	public void setOnlyUsersAndGroups(boolean onlyUsersAndGroups)
	{
		this.onlyUsersAndGroups = onlyUsersAndGroups;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		cancelGroupSelectionButton.setDisplayed(false);
		tree.registerSubInnerSection(section, id);
		section.setDialog(this);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return renderSection(context, section);
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		Collection<Button> footerActions = super.collectFooterActions(context);
		footerActions.add(cancelGroupSelectionButton);
		return footerActions;
	}

	@Override
	protected JSHandler createCancelHandler(SectionTree tree)
	{
		return events.getNamedHandler("clearUserSession");
	}

	@Override
	protected JSHandler getTemplateCloseFunction()
	{
		return events.getNamedHandler("clearUserSession");
	}

	@EventHandlerMethod
	public void clearUserSession(SectionInfo info)
	{
		section.clearUserSession(info);
		closeDialog(info);
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		return events.getSubmitValuesHandler("returnExpression");
	}

	@EventHandlerMethod
	public void returnExpression(SectionInfo info)
	{
		String expression = section.convertTreeToExpression(info);
		// this way doesn't reload the section, the following does
		closeDialog(info, new FunctionCallStatement(getOkCallback(), getSectionId(), expression));
		section.clearUserSession(info);

		// info.getRootRenderContext().setRenderedBody(
		// new CloseWindowResult(jscall(getCloseFunction()),
		// jscall(getOkCallback(), getSectionId(), expression)));
	}

	@Override
	public String getWidth()
	{
		return "1000px";
	}

	@Override
	public String getHeight()
	{
		return "550px";
	}

	public Label getTitle()
	{
		if( title == null )
		{
			title = LABEL_SELECTOR_TITLE;
		}
		return title;
	}

	public void setTitle(Label title)
	{
		this.title = title;
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return getTitle();
	}

	@Override
	public ExpressionSelectorDialogModel instantiateDialogModel(SectionInfo info)
	{
		return new ExpressionSelectorDialogModel();
	}

	public void setExpression(SectionInfo info, String expression)
	{
		section.setExpression(info, expression);
	}

	/**
	 * @param info
	 * @return the recipient expression and an empty string if nothing is
	 *         selected
	 */
	public String getExpression(SectionInfo info)
	{
		return section.getExpression(info);
	}

	public Button getCancelGroupSelectionButton()
	{
		return cancelGroupSelectionButton;
	}

	public static class ExpressionSelectorDialogModel extends DialogModel
	{
		// none
	}
}
