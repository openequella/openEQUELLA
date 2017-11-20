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

package com.tle.core.taxonomy.wizard;

import static com.tle.common.taxonomy.wizard.PopupBrowserConstants.POPUP_ALLOW_BROWSING;
import static com.tle.common.taxonomy.wizard.PopupBrowserConstants.POPUP_ALLOW_SEARCHING;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.core.taxonomy.wizard.model.BaseTermSelectorWebControlModel;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public abstract class AbstractPopupBrowserWebControl
	extends
		BaseTermSelectorDisplayDelegate<BaseTermSelectorWebControlModel>
{
	@Inject
	private ComponentFactory componentFactory;

	@Component
	@PlugKey("wizard.popupbrowser.add")
	private Link addTermLink;

	private final AbstractPopupBrowserDialog<?> popupBrowserDialog;

	AbstractPopupBrowserWebControl(AbstractPopupBrowserDialog<?> popupBrowserDialog)
	{
		this.popupBrowserDialog = popupBrowserDialog;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		popupBrowserDialog.setMultiple(isMultiple());
		popupBrowserDialog.setTaxonomyUuid(definitionControl.getSelectedTaxonomy());
		popupBrowserDialog.setOkCallback(termWebControl.getReloadFunction(true, events.getEventHandler("addTerms")));
		popupBrowserDialog.setStorageFormat(definitionControl.getTermStorageFormat());
		popupBrowserDialog.setRestriction(definitionControl.getSelectionRestriction());
		popupBrowserDialog.setBrowseEnabled(definitionControl.getBooleanAttribute(POPUP_ALLOW_BROWSING, true));
		popupBrowserDialog.setSearchEnabled(definitionControl.getBooleanAttribute(POPUP_ALLOW_SEARCHING, true));

		componentFactory.registerComponent(id, "popupBrowserDialog", tree, popupBrowserDialog);

		addTermLink.setClickHandler(popupBrowserDialog.getOpenFunction());
		addTermLink.setDisablable(true);

		termsTable.setAddAction(addTermLink);
	}

	@Override
	protected String[] getAjaxIds(String id)
	{
		return new String[]{id + "popupbrowserControl"};
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		addDisabler(context, addTermLink);
		return viewFactory.createResult("popupbrowser/selections.ftl", context);
	}

	@EventHandlerMethod
	public void addTerms(final SectionContext context, final Set<String> values) throws Exception
	{
		final BaseTermSelectorWebControlModel model = getModel(context);

		if( isMultiple() )
		{
			values.addAll(storageControl.getValues());
		}
		model.setSelectedTerms(values);
	}

	public boolean isMultiple()
	{
		return definitionControl.isAllowMultiple();
	}

	public AbstractPopupBrowserDialog<?> getPopupBrowserDialog()
	{
		return popupBrowserDialog;
	}

	@Override
	public boolean isEmpty()
	{
		return storageControl.getValues().isEmpty();
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		final BaseTermSelectorWebControlModel model = getModel(info);
		if( model.getSelectedTerms() != null )
		{
			final List<String> controlValues = storageControl.getValues();
			controlValues.clear();
			controlValues.addAll(model.getSelectedTerms());
		}
	}

	@Override
	public BaseTermSelectorWebControlModel instantiateModel(SectionInfo info)
	{
		return new BaseTermSelectorWebControlModel();
	}
}
