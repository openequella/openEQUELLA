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

package com.tle.web.connectors.manage;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.core.guice.Bind;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.bulk.operation.BulkOperationExtension.OperationInfo;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.Option;

@Bind
@NonNullByDefault
public class ConnectorBulkResultsDialog extends AbstractBulkResultsDialog<ConnectorItemKey>
{
	@PlugKey("connector.opresults.count")
	private static String OPRESULTS_COUNT_KEY;

	@Inject
	private BulkRemoveContentOperation removeOp;
	@Inject
	private BulkMoveContentOperation moveOp;

	@TreeLookup
	private AbstractBulkSelectionSection<ConnectorItemKey> selectionSection;

	@Override
	protected DynamicHtmlListModel<OperationInfo> getBulkOperationList(SectionTree tree, String parentId)
	{
		return new ConnectorBulkOperationList(tree, parentId);
	}

	public class ConnectorBulkOperationList extends DynamicHtmlListModel<OperationInfo>
	{
		private final List<BulkOperationExtension> bulkOps = new ArrayList<BulkOperationExtension>();

		public ConnectorBulkOperationList(SectionTree tree, String parentId)
		{
			bulkOps.add(removeOp);
			bulkOps.add(moveOp);
			for( BulkOperationExtension operation : bulkOps )
			{
				operation.register(tree, parentId);
			}
		}

		@Override
		protected Iterable<Option<OperationInfo>> populateOptions(SectionInfo info)
		{
			List<Option<OperationInfo>> ops = new ArrayList<Option<OperationInfo>>();
			for( BulkOperationExtension operation : bulkOps )
			{
				operation.addOptions(info, ops);
			}
			return ops;
		}

		@Nullable
		@Override
		protected Iterable<OperationInfo> populateModel(SectionInfo info)
		{
			return null;
		}
	}

	@Override
	protected Label getOpResultCountLabel(int totalSelections)
	{
		return new PluralKeyLabel(OPRESULTS_COUNT_KEY, totalSelections);
	}

	@Override
	protected List<com.tle.web.bulk.section.AbstractBulkResultsDialog.SelectionRow> getRows(
		List<ConnectorItemKey> pageOfIds)
	{
		List<SelectionRow> rows = new ArrayList<SelectionRow>();

		for( ConnectorItemKey itemId : pageOfIds )
		{
			rows.add(new SelectionRow(new TextLabel(itemId.getTitle()), new HtmlComponentState(RendererConstants.LINK,
				events.getNamedHandler("removeSelection", itemId)))); //$NON-NLS-1$
		}
		return rows;
	}

	@EventHandlerMethod
	public void removeSelection(SectionInfo info, String itemId)
	{
		selectionSection.removeSelection(info, new ConnectorItemKey(itemId));
	}
}
