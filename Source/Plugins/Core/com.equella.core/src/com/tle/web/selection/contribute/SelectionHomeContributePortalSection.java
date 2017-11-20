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

package com.tle.web.selection.contribute;

import java.util.Set;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.guice.Bind;
import com.tle.core.quickupload.service.QuickUploadService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.Box;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.wizard.WebWizardService;
import com.tle.web.wizard.WizardConstants;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class SelectionHomeContributePortalSection extends AbstractPrototypeSection<QuickUploadContributeModel>
	implements
		HtmlRenderer,
		ViewableChildInterface
{
	@PlugKey("selcontribute.title")
	private static Label LABEL_TITLE;
	@PlugKey("contributebutton")
	private static Label LABEL_CONTRIBUTEBUTTON;

	@Component
	private Box box;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;
	@Component
	private Button contributeButton;

	@Inject
	private QuickUploadService quickUploadService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private WebWizardService webWizardService;
	@Inject
	private QuickUploadSection qus;

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( canView(context) )
		{
			return viewFactory.createResult("quickuploadandcontribute.ftl", context);
		}

		return null;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		ItemDefinition itemdef = quickUploadService.getOneClickItemDef();
		SelectionSession css = selectionService.getCurrentSession(info);
		if( css == null )
		{
			return false;
		}

		boolean quick = false;
		boolean cont = false;
		if( itemdef != null
			&& (css.isAllContributionCollections() || css.getContributionCollectionIds().contains(itemdef.getUuid())) )
		{
			quick = true;
		}
		if( css.isAllContributionCollections() || !css.getContributionCollectionIds().isEmpty() )
		{
			cont = true;
		}

		QuickUploadContributeModel model = getModel(info);
		model.setQuickUpload(quick);
		model.setContribute(cont);

		return quick || cont;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(qus, id);

		box.setNoMinMaxOnHeader(true);
		box.setLabel(LABEL_TITLE);

		contributeButton.setClickHandler(events.getNamedHandler("contribute"));
		contributeButton.setLabel(LABEL_CONTRIBUTEBUTTON);
	}

	@EventHandlerMethod
	public void contribute(SectionContext context) throws Exception
	{
		SelectionSession session = selectionService.getCurrentSession(context);
		final Set<String> collections = session.getContributionCollectionIds();
		if( collections.size() == 1 )
		{
			webWizardService.forwardToNewItemWizard(context, collections.iterator().next(), null, null, false);
		}
		else
		{
			context.forward(context.createForward(WizardConstants.CONTRIBUTE_URL));
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "quc";
	}

	@Override
	public Class<QuickUploadContributeModel> getModelClass()
	{
		return QuickUploadContributeModel.class;
	}

	public Box getBox()
	{
		return box;
	}

	public Button getContributeButton()
	{
		return contributeButton;
	}

	@NonNullByDefault(false)
	public static class ContentFields
	{
		private String title;
		private String description;

		public String getTitle()
		{
			return title;
		}

		public void setTitle(String title)
		{
			this.title = title;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}
	}

	public QuickUploadSection getQus()
	{
		return qus;
	}
}
