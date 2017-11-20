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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.edge.common.Constants;
import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.common.taxonomy.wizard.AutocompleteEditBoxConstants;
import com.tle.common.taxonomy.wizard.TermSelectorControl.TermStorageFormat;
import com.tle.core.guice.Bind;
import com.tle.core.taxonomy.AutoCompleteTermResult;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.wizard.model.BaseTermSelectorWebControlModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SimpleSectionId;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class AutocompleteEditBoxWebControl
	extends
		BaseTermSelectorDisplayDelegate<AutocompleteEditBoxWebControl.AutocompleteEditBoxModel>
{
	private static final int RESULT_LIMIT = 20;

	@PlugKey("wizard.autocompleteeditbox.error.noadd")
	private static Label LABEL_ERROR_NOADD;
	@PlugKey("wizard.autocompleteeditbox.error.notleaf")
	private static Label LABEL_ERROR_NOTLEAF;

	@Inject
	private TaxonomyService taxonomyService;

	@Component(name = "e")
	private TextField editbox;
	@Component(name = "s")
	@PlugKey("wizard.autocompleteeditbox.selectthisterm")
	private Button selectTerm;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		AutocompleteEditBoxModel model = getModel(context);

		boolean showEditbox = (definitionControl.isAllowMultiple() || storageControl.isEmpty());
		if( showEditbox )
		{
			model.setCanSelect(true);
			addDisablers(context, selectTerm, editbox);
		}

		return viewFactory.createResult("autocompleteeditbox/autocompleteeditbox.ftl", context);
	}

	@AjaxMethod
	public AutoCompleteTermResult[] autocomplete(SectionInfo info)
	{
		final SelectionRestriction selection = definitionControl.getSelectionRestriction();
		// TODO: this requires some thought...
		final boolean searchFullTerm = true;
		final String query = (searchFullTerm ? '*' : "") + editbox.getValue(info) + '*';

		final Pair<Long, List<TermResult>> res = taxonomyService.searchTerms(definitionControl.getSelectedTaxonomy(),
			query, selection, RESULT_LIMIT, searchFullTerm);

		// The terms must be sorted, there are no promises from the taxonomy
		// service about the order, and the List implementation may not support
		// modifications, so create our own copy.
		final List<TermResult> terms = Lists.newArrayList(res.getSecond());
		Collections.sort(terms, new NumberStringComparator<TermResult>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String convertToString(TermResult tr)
			{
				// sort by full term only if searching by full term...
				// TODO: again, this requires some thought...
				return (searchFullTerm ? tr.getFullTerm() : tr.getTerm());
			}
		});

		Collection<AutoCompleteTermResult> suggestTerms = Lists.newArrayList();

		for( TermResult term : terms )
		{
			StringBuilder result = new StringBuilder();
			result.append(term.getFullTerm());
			suggestTerms.add(new AutoCompleteTermResult(term.getTerm() + " - " + result.toString(), result.toString()));
		}

		return suggestTerms.toArray(new AutoCompleteTermResult[suggestTerms.size()]);
	}

	@EventHandlerMethod
	public void termSelected(SectionInfo info)
	{
		final AutocompleteEditBoxModel model = getModel(info);
		String fullTermPath = editbox.getValue(info);

		if( !Check.isEmpty(fullTermPath) )
		{
			if( fullTermPath.startsWith(TaxonomyConstants.TERM_SEPARATOR) && fullTermPath.length() > 1 )
			{
				fullTermPath = fullTermPath.substring(TaxonomyConstants.TERM_SEPARATOR.length());
			}
			final String taxonomyUuid = definitionControl.getSelectedTaxonomy();
			final Pair<String, String> termBits = getTermComponents(fullTermPath);
			final String parentFullPath = termBits.getFirst();
			final String termName = termBits.getSecond();

			final TermResult term = taxonomyService.getTerm(taxonomyUuid, fullTermPath);
			if( term == null )
			{
				if( definitionControl.isAllowAddTerms() && taxonomyService.supportsTermAddition(taxonomyUuid) )
				{
					try
					{
						taxonomyService.validateTerm(taxonomyUuid, parentFullPath, termName, false);
					}
					catch( InvalidDataException d )
					{
						for( ValidationError error : d.getErrors() )
						{
							model.setErrorMessage(new TextLabel(error.getMessage()));
							return;
						}
					}

					TermResult added = null;
					try
					{
						added = taxonomyService.addTerm(taxonomyUuid, parentFullPath, termName, true);
						fullTermPath = added.getFullTerm();
					}
					catch( InvalidDataException d )
					{
						for( ValidationError error : d.getErrors() )
						{
							model.setErrorMessage(new TextLabel(error.getMessage()));
							return;
						}
					}
				}
				else
				{
					model.setErrorMessage(LABEL_ERROR_NOADD);
					return;
				}
			}
			else if( !term.isLeaf() && definitionControl.getSelectionRestriction() == SelectionRestriction.LEAF_ONLY )
			{
				model.setErrorMessage(LABEL_ERROR_NOTLEAF);
				return;
			}

			editbox.setValue(info, Constants.BLANK);
			model.setSelectedTerms(Collections.singleton(
				definitionControl.getTermStorageFormat() == TermStorageFormat.LEAF_ONLY ? termName : fullTermPath));
		}
	}

	/**
	 * @return The parent path and the term value
	 */
	private Pair<String, String> getTermComponents(String fullTermPath)
	{
		String parentFullPath = "";
		final String termValue;

		final int ind = fullTermPath.lastIndexOf(TaxonomyConstants.TERM_SEPARATOR);
		if( ind >= 0 )
		{
			parentFullPath = fullTermPath.substring(0, ind);
			termValue = fullTermPath.substring(ind + 1);
		}
		else
		{
			termValue = fullTermPath;
		}

		return new Pair<String, String>(parentFullPath, termValue);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		String ajaxDivId = id + "autocompleteControl";

		// if is reload page
		if( definitionControl.getBooleanAttribute(AutocompleteEditBoxConstants.RELOAD_PAGE_ON_SELECTION, false) )
		{
			selectTerm.setClickHandler(termWebControl.getReloadFunction(true, events.getEventHandler("termSelected")));
		}
		else
		{
			selectTerm.setClickHandler(
				ajax.getAjaxUpdateDomFunction(tree, new SimpleSectionId(id), events.getEventHandler("termSelected"),
					ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), ajaxDivId));
		}

		editbox.setAutoCompleteCallback(ajax.getAjaxFunction("autocomplete"));
		editbox.addEventStatements("autoselect", selectTerm.getClickHandler());
	}

	@Override
	protected String[] getAjaxIds(String id)
	{
		return new String[]{id + "autocompleteControl"};
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		final List<String> values = storageControl.getValues();
		final Set<String> selections = getModel(info).getSelectedTerms();
		if( !Check.isEmpty(selections) )
		{
			for( String s : selections )
			{
				if( !values.contains(s) )
				{
					values.add(s);
				}
			}
		}
	}

	@Override
	public boolean isEmpty()
	{
		return storageControl.isEmpty();
	}

	@Override
	public AutocompleteEditBoxModel instantiateModel(SectionInfo info)
	{
		return new AutocompleteEditBoxModel();
	}

	public static class AutocompleteEditBoxModel extends BaseTermSelectorWebControlModel
	{
		private Label errorMessage;
		private boolean canSelect;

		public Label getErrorMessage()
		{
			return errorMessage;
		}

		public void setErrorMessage(Label errorMessage)
		{
			this.errorMessage = errorMessage;
		}

		public boolean isCanSelect()
		{
			return canSelect;
		}

		public void setCanSelect(boolean canSelect)
		{
			this.canSelect = canSelect;
		}
	}

	public TextField getEditbox()
	{
		return editbox;
	}

	public Button getSelectTerm()
	{
		return selectTerm;
	}
}
