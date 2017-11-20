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

package com.tle.mycontent.web.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.mycontent.ContentHandler;
import com.tle.mycontent.service.MyContentService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.template.Decorations;

/**
 * @author Aaron
 */
public class MyContentHandlerDelegationSection
	extends
		AbstractPrototypeSection<MyContentHandlerDelegationSection.MyContentHandlerDelegationModel>
	implements
		HtmlRenderer
{
	@Inject
	private MyContentService myContentService;

	@TreeLookup
	private MyContentContributeSection contrib;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		MyContentHandlerDelegationModel model = getModel(context);
		ContentHandler handler = myContentService.getHandlerForId(contrib.getModel(context).getContributeId());
		if( handler == null )
		{
			throw new SectionsRuntimeException("No handler id specified");
		}
		model.setHandlerRenderer(handler.render(context));
		Label title = handler.getTitle(context);
		model.setTitle(title);
		Decorations.getDecorations(context).setTitle(title);

		final GenericTemplateResult result = new GenericTemplateResult();
		result.addNamedResult("left", view.createResult("handler.ftl", this));

		final List<HtmlComponentState> majorActions = handler.getMajorActions(context);
		if( majorActions != null )
		{
			for( HtmlComponentState action : majorActions )
			{
				action.setRendererType(EquellaButtonExtension.ACTION_BUTTON);
			}
		}
		final MyContentHandlerDelegationActionsModel actionsModel = new MyContentHandlerDelegationActionsModel();
		actionsModel.setMajorActions(majorActions);
		actionsModel.setMinorActions(handler.getMinorActions(context));

		result.addNamedResult("right", view.createResultWithModel("handleractions.ftl", actionsModel));
		return result;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new MyContentHandlerDelegationModel();
	}

	public static class MyContentHandlerDelegationModel
	{
		private SectionRenderable handlerRenderer;
		private Label title;

		public Label getTitle()
		{
			return title;
		}

		public void setTitle(Label title)
		{
			this.title = title;
		}

		public SectionRenderable getHandlerRenderer()
		{
			return handlerRenderer;
		}

		public void setHandlerRenderer(SectionRenderable handlerRenderer)
		{
			this.handlerRenderer = handlerRenderer;
		}
	}

	public static class MyContentHandlerDelegationActionsModel
	{
		private List<HtmlComponentState> majorActions;
		private List<HtmlComponentState> minorActions;

		public List<HtmlComponentState> getMajorActions()
		{
			return majorActions;
		}

		public void setMajorActions(List<HtmlComponentState> majorActions)
		{
			this.majorActions = majorActions;
		}

		public List<HtmlComponentState> getMinorActions()
		{
			return minorActions;
		}

		public void setMinorActions(List<HtmlComponentState> minorActions)
		{
			this.minorActions = minorActions;
		}
	}
}
