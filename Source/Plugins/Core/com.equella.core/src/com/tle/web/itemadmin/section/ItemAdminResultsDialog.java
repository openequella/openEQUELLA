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

package com.tle.web.itemadmin.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.bulk.operation.BulkOperationExtension.OperationInfo;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.itemadmin.WithinEntry;
import com.tle.web.itemadmin.operation.BulkAddCollaboratorsDialog;
import com.tle.web.itemadmin.operation.BulkChangeOwnerDialog;
import com.tle.web.itemadmin.operation.StandardOperations;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.SpanRenderer;

@Bind
@NonNullByDefault
public class ItemAdminResultsDialog extends AbstractBulkResultsDialog<ItemIdKey>
{
	@PlugKey("itemadmin.opresults.count")
	private static String OPRESULTS_COUNT_KEY;
	@PlugKey("error.personal")
	private static Label ERROR_SCRAPBOOK;

	@Inject
	private ItemService itemService;
	@Inject
	private StandardOperations standardOps;
	@Inject
	private BulkChangeOwnerDialog changeOwnerOp;
	@Inject
	private BulkAddCollaboratorsDialog addCollabsOp;
	@Inject
	private BundleCache bundleCache;

	@TreeLookup
	private AbstractBulkSelectionSection<ItemIdKey> selectionSection;
	@TreeLookup
	private ItemAdminQuerySection querySection;

	private PluginTracker<BulkOperationExtension> tracker;

	@EventHandlerMethod
	public void removeSelection(SectionInfo info, long itemId, String uuid, int version)
	{
		selectionSection.removeSelection(info, new ItemIdKey(itemId, uuid, version));
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		tracker = new PluginTracker<BulkOperationExtension>(pluginService, "com.tle.web.itemadmin", "bulkExtension", null)
			.setBeanKey("bean");
	}

	public class BulkOperationList extends DynamicHtmlListModel<OperationInfo>
	{
		private final List<BulkOperationExtension> bulkOps = new ArrayList<BulkOperationExtension>();

		public BulkOperationList(SectionTree tree, String parentId)
		{
			bulkOps.add(standardOps);
			bulkOps.add(changeOwnerOp);
			bulkOps.add(addCollabsOp);
			bulkOps.addAll(tracker.getNewBeanList());
			for( BulkOperationExtension op : bulkOps )
			{
				op.register(tree, parentId);
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

		@Override
		protected Iterable<OperationInfo> populateModel(SectionInfo info)
		{

			return null;
		}
	}

	@Override
	protected SectionRenderable renderOptions(RenderContext context, OperationInfo opInfo)
	{
		WithinEntry selected = querySection.getCollectionList().getSelectedValue(context);

		if( selected != null && selected.isSimpleOpsOnly() )
		{
			getPrevButton().setDisplayed(context, true);
			getModel(context).setErrored(true);
			return new SpanRenderer(ERROR_SCRAPBOOK).addClass("simpleopsmessage");
		}

		return super.renderOptions(context, opInfo);
	}

	@Override
	protected DynamicHtmlListModel<OperationInfo> getBulkOperationList(SectionTree tree, String parentId)
	{

		return new BulkOperationList(tree, parentId);
	}

	@Override
	protected Label getOpResultCountLabel(int totalSelections)
	{

		return new PluralKeyLabel(OPRESULTS_COUNT_KEY, totalSelections);
	}

	@Override
	protected List<SelectionRow> getRows(List<ItemIdKey> pageOfIds)
	{
		List<SelectionRow> rows = new ArrayList<SelectionRow>();
		Map<ItemId, LanguageBundle> itemNames = itemService.getItemNames(pageOfIds);
		for( ItemIdKey itemId : pageOfIds )
		{
			rows.add(new SelectionRow(new BundleLabel(itemNames.get(ItemId.fromKey(itemId)), itemId.getUuid(),
				bundleCache), new HtmlComponentState(RendererConstants.LINK, events.getNamedHandler(
				"removeSelection", itemId.getKey(), itemId.getUuid(), itemId.getVersion())))); //$NON-NLS-1$
		}
		return rows;
	}

	@Override
	protected List<ItemIdKey> getItemIds(List<Long> longs)
	{
		if( longs.size() > 50 )
		{
			throw new RuntimeException("Too many items");
		}
		if( Check.isEmpty(longs) )
		{
			return Collections.emptyList();
		}
		return itemService.getItemIdKeys(longs);
	}
}
