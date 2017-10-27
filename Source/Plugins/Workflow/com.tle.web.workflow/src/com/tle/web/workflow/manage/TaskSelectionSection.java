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

package com.tle.web.workflow.manage;

import java.util.Set;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.search.DefaultSearch;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.item.TaskResult;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.annotations.Component;

@NonNullByDefault
public class TaskSelectionSection extends AbstractBulkSelectionSection<ItemTaskId>
{
	private static final String KEY_SELECTIONS = "taskSelections";

	@PlugKey("selectionsbox.selectall")
	private static Label LABEL_SELECTALL;
	@PlugKey("selectionsbox.unselect")
	private static Label LABEL_UNSELECTALL;
	@PlugKey("selectionsbox.viewselected")
	private static Label LABEL_VIEWSELECTED;
	@PlugKey("selectionsbox.pleaseselect")
	private static Label LABEL_PLEASE;
	@PlugKey("selectionsbox.count")
	private static String LABEL_COUNT;

	@Component
	@Inject
	private TaskResultsDialog bulkDialog;
	@Inject
	private FreeTextService freeTextService;

	@TreeLookup
	private AbstractFreetextResultsSection<?, ?> resultsSection;

	@Override
	public void selectAll(SectionInfo info)
	{
		FreetextSearchEvent searchEvent = resultsSection.createSearchEvent(info);
		info.processEvent(searchEvent);
		DefaultSearch search = searchEvent.getFinalSearch();
		FreetextSearchResults<TaskResult> results = freeTextService.search(search, 0, Integer.MAX_VALUE);
		Model<ItemTaskId> model = getModel(info);
		Set<ItemTaskId> selections = model.getSelections();

		int count = results.getCount();
		for( int i = 0; i < count; i++ )
		{
			TaskResult itemId = results.getResultData(i);
			selections.add(new ItemTaskId(itemId.getItemIdKey(), itemId.getTaskId()));
		}
		model.setModifiedSelection(true);
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
	protected String getKeySelections()
	{
		return KEY_SELECTIONS;
	}

	@Override
	protected AbstractBulkResultsDialog<ItemTaskId> getBulkDialog()
	{
		return bulkDialog;
	}

	@Override
	protected boolean useBitSet()
	{
		return false;
	}

}
