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

package com.tle.web.copyright.itemlist;

import java.util.List;
import java.util.Map;

import com.dytech.edge.common.Constants;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.web.itemlist.StdMetadataEntry;
import com.tle.web.itemlist.item.AbstractItemlikeListEntry;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;

public abstract class AbstractCitationGenerator<H extends Holding, P extends Portion, S extends Section>
	implements
		ItemlikeListEntryExtension<Item, ItemListEntry>
{
	private static PluginResourceHelper RESOURCES = ResourcesService.getResourceHelper(AbstractCitationGenerator.class);

	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(RenderContext context, List<ItemListEntry> entries,
		ListSettings<ItemListEntry> listSettings)
	{
		List<Item> items = AbstractItemlikeListEntry.getItems(entries);
		final CopyrightService<H, P, S> copyrightService = getCopyrightService();
		final Map<Long, H> holdingsMap = copyrightService.getHoldingsForItems(items);
		final Map<Long, List<P>> portionsMap = copyrightService.getPortionsForItems(items);

		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@Override
			public void processEntry(ItemListEntry entry)
			{
				String citation = Constants.BLANK;

				Item item = entry.getItem();

				long id = item.getId();
				H holding = holdingsMap.get(id);
				if( holding != null )
				{
					List<P> portions = portionsMap.get(id);
					P portion = null;

					if( portions != null )
					{
						portion = portions.get(0);
					}

					citation = copyrightService.citate(holding, portion);

					if( !Check.isEmpty(citation) )
					{
						TextLabel label = new TextLabel(citation, true);
						entry.addMetadata(new StdMetadataEntry(
							new KeyLabel(RESOURCES.key("list.citation")), new LabelRenderer(label))); //$NON-NLS-1$
					}
				}
			}
		};
	}

	public abstract CopyrightService<H, P, S> getCopyrightService();
}