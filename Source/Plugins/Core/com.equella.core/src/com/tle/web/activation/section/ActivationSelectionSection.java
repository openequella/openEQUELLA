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

package com.tle.web.activation.section;

import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.item.ItemActivationId;
import com.tle.common.search.DefaultSearch;
import com.tle.core.activation.ActivationResult;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.annotations.Component;

public class ActivationSelectionSection extends AbstractBulkSelectionSection<ItemActivationId>
{
	private static final String KEY_SELECTIONS = "activationSelections"; //$NON-NLS-1$

	@PlugKey("activations.selectionsbox.selectall")
	private static Label LABEL_SELECTALL;
	@PlugKey("activations.selectionsbox.unselect")
	private static Label LABEL_UNSELECTALL;
	@PlugKey("activations.selectionsbox.viewselected")
	private static Label LABEL_VIEWSELECTED;
	@PlugKey("activations.selectionsbox.pleaseselect")
	private static Label LABEL_PLEASE;
	@PlugKey("activations.selectionsbox.count")
	private static String LABEL_COUNT;

	@Inject
	@Component
	private ActivationResultsDialog bulkDialog;
	@Inject
	private FreeTextService freeTextService;

	@TreeLookup
	protected AbstractFreetextResultsSection<?, ?> resultsSection;

	@Override
	@EventHandlerMethod
	public void selectAll(SectionInfo info)
	{
		FreetextSearchEvent searchEvent = resultsSection.createSearchEvent(info);
		info.processEvent(searchEvent);
		DefaultSearch search = searchEvent.getFinalSearch();
		FreetextSearchResults<ActivationResult> results = freeTextService.search(search, 0, Integer.MAX_VALUE);
		Model<ItemActivationId> model = getModel(info);
		Set<ItemActivationId> selections = model.getSelections();

		int count = results.getCount();
		for( int i = 0; i < count; i++ )
		{
			ActivationResult itemId = results.getResultData(i);
			selections.add(new ItemActivationId(itemId.getItemIdKey(), itemId.getActivationId()));
		}
		model.setModifiedSelection(true);
	}

	@Override
	protected String getKeySelections()
	{
		return KEY_SELECTIONS;
	}

	@Override
	protected Label getLabelSelectAll()
	{
		return LABEL_SELECTALL;
	}

	@Override
	protected Label getLabelUnselectAll()
	{
		return LABEL_UNSELECTALL;
	}

	@Override
	protected Label getLabelViewSelected()
	{
		return LABEL_VIEWSELECTED;
	}

	@Override
	protected Label getPleaseSelectLabel()
	{
		return LABEL_PLEASE;
	}

	@Override
	protected Label getSelectionBoxCountLabel(int selectionCount)
	{
		return new PluralKeyLabel(LABEL_COUNT, selectionCount);
	}

	@Override
	protected AbstractBulkResultsDialog<ItemActivationId> getBulkDialog()
	{
		return bulkDialog;
	}

	@Override
	protected boolean useBitSet()
	{
		return false;
	}
}
