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

package com.tle.web.portal.standard.editor;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import net.sf.json.JSONArray;

import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.function.PassThroughFunction;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.ParentFrameSelectionCallback;
import com.tle.web.selection.SelectedResourceDetails;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.RenderTemplate;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class ShowcasePortletEditorSection
	extends
		AbstractPortletEditorSection<ShowcasePortletEditorSection.ShowcasePortletEditorModel>
{
	private static final String TYPE = "showcase";

	@Inject
	private SelectionService selectionService;

	@ViewFactory
	private FreemarkerFactory thisView;
	@EventFactory
	private EventGenerator events;

	@Component(name = "s")
	private Button searchButton;

	private PassThroughFunction resultsCallback;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		searchButton.setClickHandler(events.getNamedHandler("select"));
		resultsCallback = new PassThroughFunction("results" + id, events.getSubmitValuesFunction("results"));
	}

	@EventHandlerMethod
	@SuppressWarnings("unchecked")
	public void results(SectionInfo info, String results)
	{
		JSONArray array = JSONArray.fromObject(results);
		Collection<SelectedResourceDetails> selectedResources = JSONArray.toCollection(array,
			SelectedResourceDetails.class);
		getModel(info).setSelectedResources(selectedResources);
	}

	@EventHandlerMethod
	public void select(SectionInfo info)
	{
		SelectionSession session = new SelectionSession(new ParentFrameSelectionCallback(resultsCallback, true));
		session.setSelectAttachments(true);
		session.setSelectItem(true);
		session.setSelectPackage(true);
		session.setSelectMultiple(false);
		session.setAllCollections(true);

		RenderTemplate renderTemplate = info.lookupSection(RenderTemplate.class);
		renderTemplate.setHideBanner(info, true);
		renderTemplate.setHideNavigation(info, true);
		selectionService.forwardToNewSession(info, session, null);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "scs";
	}

	@Override
	protected Portlet createNewPortlet()
	{
		return new Portlet(TYPE);
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		// Nothing by default
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		// Nothing by default
	}

	@Override
	protected SectionRenderable customRender(RenderEventContext context, ShowcasePortletEditorModel model,
		PortletEditingBean portlet) throws Exception
	{
		return thisView.createResult("edit/editshowcaseportlet.ftl", context);
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portletPack)
	{
		// Nothing by default
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		// Nothing by default
	}

	@Override
	public Class<ShowcasePortletEditorModel> getModelClass()
	{
		return ShowcasePortletEditorModel.class;
	}

	public Button getSearchButton()
	{
		return searchButton;
	}

	public PassThroughFunction getResultsCallback()
	{
		return resultsCallback;
	}

	public static class ShowcasePortletEditorModel extends AbstractPortletEditorSection.AbstractPortletEditorModel
	{
		private Collection<SelectedResourceDetails> selectedResources;

		public Collection<SelectedResourceDetails> getSelectedResources()
		{
			return selectedResources;
		}

		public void setSelectedResources(Collection<SelectedResourceDetails> selectedResources)
		{
			this.selectedResources = selectedResources;
		}
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
