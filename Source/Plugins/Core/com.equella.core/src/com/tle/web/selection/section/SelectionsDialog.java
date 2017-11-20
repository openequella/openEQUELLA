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

package com.tle.web.selection.section;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.section.SelectionsDialog.ItemVersionSelectionModel;

@Bind
@SuppressWarnings("nls")
public class SelectionsDialog extends AbstractOkayableDialog<ItemVersionSelectionModel>
{
	@PlugKey("versiondialog.title")
	private static Label DIALOG_TITLE_LABEL;

	@Inject
	private SelectionService selectionService;
	@Inject
	private VersionSelectionSection versionSelectionSection;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	public SelectionsDialog()
	{
		setAjax(true);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		final ItemVersionSelectionModel model = getModel(context);

		String courseName = selectionService.getCurrentSession(context).getStructure().getName();
		model.setCourseTitle(courseName);

		return viewFactory.createResult("selection/dialog/selections.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		versionSelectionSection.setAjaxDivId("table-div");
		tree.registerInnerSection(versionSelectionSection, id);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return DIALOG_TITLE_LABEL;
	}

	@Override
	public ItemVersionSelectionModel instantiateDialogModel(SectionInfo info)
	{
		return new ItemVersionSelectionModel();
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		return events.getNamedHandler("saveVersions");
	}

	@EventHandlerMethod
	public void saveVersions(SectionInfo info)
	{
		versionSelectionSection.saveVersionChoices(info);
		closeDialog(info, getOkCallback());
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("showFolder");
	}

	@EventHandlerMethod
	public void showFolder(SectionInfo info, String folderId, boolean showAll)
	{
		versionSelectionSection.setFolder(info, folderId, showAll);
		super.showDialog(info);
	}

	@Override
	public String getHeight()
	{
		return "400px";
	}

	@Override
	public String getWidth()
	{
		return "725px";
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "vsdialog";
	}

	public VersionSelectionSection getVersionSelectionSection()
	{
		return versionSelectionSection;
	}

	public static class ItemVersionSelectionModel extends DialogModel
	{
		private String courseTitle;

		public String getCourseTitle()
		{
			return courseTitle;
		}

		public void setCourseTitle(String courseTitle)
		{
			this.courseTitle = courseTitle;
		}
	}
}
