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

import java.util.List;

import com.tle.common.taxonomy.wizard.AutocompleteEditBoxConstants;
import com.tle.core.taxonomy.wizard.model.BaseTermSelectorWebControlModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class BaseTermSelectorDisplayDelegate<M extends BaseTermSelectorWebControlModel>
	extends
		TermSelectorDisplayDelegate<M>
{
	@PlugKey("currentlyselectedstuff.nothing")
	private static Label NO_TERMS_SELECTED_LABEL;
	@PlugKey("currentlyselectedstuff.remove")
	private static Label REMOVE_TERM_LABEL;
	@PlugKey("wizard.basetermselector.confirmremove")
	private static String CONFIRM_REMOVE_TERM_KEY;

	@ViewFactory(fixed = false)
	protected FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;
	@AjaxFactory
	protected AjaxGenerator ajax;

	@Component(name = "t")
	protected SelectionsTable termsTable;

	private JSCallable removeFunction;

	protected abstract String[] getAjaxIds(String id);

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		removeFunction = ajax.getAjaxUpdateDomFunction(getTree(), BaseTermSelectorDisplayDelegate.this,
			events.getEventHandler("removeTerm"), ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING),
			getAjaxIds(id));

		termsTable.setNothingSelectedText(NO_TERMS_SELECTED_LABEL);
		termsTable.setSelectionsModel(new DynamicSelectionsTableModel<String>()
		{
			@Override
			protected List<String> getSourceList(SectionInfo info)
			{
				return storageControl.getValues();
			}

			@Override
			protected void transform(SectionInfo info, SelectionsTableSelection selection, String term,
				List<SectionRenderable> actions, int index)
			{
				selection.setName(new TextLabel(term));
				if( storageControl.isEnabled() )
				{
					JSHandler removeHandler;

					// if reload page on selection
					if( definitionControl.getBooleanAttribute(AutocompleteEditBoxConstants.RELOAD_PAGE_ON_SELECTION,
						false) )
					{
						removeHandler = new StatementHandler(termWebControl.getReloadFunction(true,
							events.getEventHandler("removeTerm")), term);
					}
					else
					{
						removeHandler = new OverrideHandler(removeFunction, term);
					}
					removeHandler.addValidator(new Confirm(new KeyLabel(CONFIRM_REMOVE_TERM_KEY, term)));
					actions.add(makeRemoveAction(REMOVE_TERM_LABEL, removeHandler));
				}
			}
		});
	}

	@EventHandlerMethod
	public void removeTerm(final SectionContext context, final String term) throws Exception
	{
		storageControl.getValues().remove(term);
	}

	public SelectionsTable getTermsTable()
	{
		return termsTable;
	}
}
