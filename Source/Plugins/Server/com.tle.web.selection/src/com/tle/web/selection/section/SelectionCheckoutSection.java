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

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class SelectionCheckoutSection extends AbstractPrototypeSection<SelectionCheckoutSection.SelectionCheckoutModel>
	implements
		HtmlRenderer
{
	@PlugKey("checkout.pagetitle")
	private static Label TITLE_LABEL;
	@PlugKey("checkout.cancel.confirm")
	private static Label CONFIRM_CANCEL;

	@PlugKey("checkout.continue.multiple")
	private static Label CONTINUE_MULTIPLE_LABEL;
	@PlugKey("checkout.continue.single")
	private static Label CONTINUE_SINGLE_LABEL;
	@PlugKey("checkout.finish.multiple")
	private static Label FINISH_MULTIPLE_LABEL;
	@PlugKey("checkout.finish.single")
	private static Label FINISH_SINGLE_LABEL;

	@Inject
	private SelectionService selectionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component
	private Button continueButton;
	@Component
	@PlugKey("checkout.cancel")
	private Button cancelButton;
	@Component
	private Button finishButton;
	@Inject
	private VersionSelectionSection versionSelectionSection;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		versionSelectionSection.setAjaxDivId("checkout-div");
		tree.registerInnerSection(versionSelectionSection, id);
		continueButton.setClickHandler(events.getNamedHandler("continueSelections"));
		cancelButton.setClickHandler(events.getNamedHandler("cancelled").addValidator(new Confirm(CONFIRM_CANCEL)));
		finishButton.setClickHandler(events.getNamedHandler("finished"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.getDecorations(context).setTitle(TITLE_LABEL);

		final SelectionSession ss = selectionService.getCurrentSession(context);
		continueButton.setLabel(context, ss.isSelectMultiple() ? CONTINUE_MULTIPLE_LABEL : CONTINUE_SINGLE_LABEL);
		finishButton.setLabel(context, ss.isSelectMultiple() ? FINISH_MULTIPLE_LABEL : FINISH_SINGLE_LABEL);

		if( ss.getSelectedResources().size() == 0 )
		{
			finishButton.setDisabled(context, true);
		}

		final SelectionCheckoutModel model = getModel(context);
		model.setCancelDisabled(ss.isCancelDisabled());

		return viewFactory.createResult("selection/checkout.ftl", context);
	}

	@EventHandlerMethod
	public void finished(SectionInfo info)
	{
		versionSelectionSection.saveVersionChoices(info);
		selectionService.returnFromSession(info);
	}

	@EventHandlerMethod
	public void continueSelections(SectionInfo info)
	{
		SelectionSession ss = selectionService.getCurrentSession(info);
		if( !ss.isSelectMultiple() )
		{
			ss.clearResources();
		}
		else
		{
			versionSelectionSection.saveVersionChoices(info);
		}

		info.forwardToUrl(getModel(info).getContinueSelectionsBackTo());
	}

	@EventHandlerMethod
	public void cancelled(SectionInfo info)
	{
		selectionService.getCurrentSession(info).clearResources();
		selectionService.returnFromSession(info);
	}

	public void setContinueSelectionsBackTo(SectionInfo info, SectionInfo fwdInfo)
	{
		getModel(info).setContinueSelectionsBackTo(fwdInfo.getPublicBookmark().getHref());
	}

	// Why?
	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new SelectionCheckoutModel();
	}

	public Button getFinishButton()
	{
		return finishButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public Button getContinueButton()
	{
		return continueButton;
	}

	public VersionSelectionSection getVersionSelectionSection()
	{
		return versionSelectionSection;
	}

	public static class SelectionCheckoutModel
	{
		@Bookmarked
		private String continueSelectionsBackTo;
		private boolean cancelDisabled;

		public String getContinueSelectionsBackTo()
		{
			return continueSelectionsBackTo;
		}

		public void setContinueSelectionsBackTo(String continueSelectionsBackTo)
		{
			this.continueSelectionsBackTo = continueSelectionsBackTo;
		}

		public boolean isCancelDisabled()
		{
			return cancelDisabled;
		}

		public void setCancelDisabled(boolean cancelDisabled)
		{
			this.cancelDisabled = cancelDisabled;
		}
	}
}
