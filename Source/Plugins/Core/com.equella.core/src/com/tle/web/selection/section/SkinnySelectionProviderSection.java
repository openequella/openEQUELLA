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

import static com.tle.web.selection.section.RootSelectionSection.Layout.SKINNY;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.item.service.ItemService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.event.ItemSelectorEvent;
import com.tle.web.selection.event.ItemSelectorEventListener;
import com.tle.web.viewurl.ItemSectionInfo;

/**
 * Normally the "select this resource" button is on the right hand side, however
 * in skinny sessions it's on the item summary, hence this class
 * 
 * @author Aaron
 */
@NonNullByDefault
@SuppressWarnings("nls")
public class SkinnySelectionProviderSection
	extends
		AbstractPrototypeSection<SkinnySelectionProviderSection.SkinnySelectionProviderModel>
	implements
		ItemSelectorEventListener
{
	@Inject
	private SelectionService selectionService;
	@Inject
	private ItemService itemService;

	@EventFactory
	private EventGenerator events;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.addListener(null, ItemSelectorEventListener.class, this);
	}

	@EventHandlerMethod
	public void selectItemSummary(SectionInfo info, ItemId itemId)
	{
		final Item item;
		ItemSectionInfo itemInfo = info.getAttribute(ItemSectionInfo.class);
		if( itemInfo != null )
		{
			item = itemInfo.getItem();
		}
		else
		{
			item = itemService.get(itemId);
		}
		selectionService.addSelectedItem(info, item, null, null);
	}

	private boolean isApplicable(SectionInfo info)
	{
		final SkinnySelectionProviderModel model = getModel(info);
		final Boolean applicable = model.getApplicable();
		if( applicable != null )
		{
			return applicable;
		}

		boolean applies = false;
		final SelectionSession ss = selectionService.getCurrentSession(info);
		if( ss != null )
		{
			applies = (ss.isSelectItem() && ss.getLayout() == SKINNY);
			// String home = ss.getHomeSelectable();
			// if( home != null && home.equals("skinnysearch") )
			// {
			// applies = true;
			// }
		}
		model.setApplicable(applies);
		return applies;
	}

	@Override
	public void supplyFunction(SectionInfo info, ItemSelectorEvent event)
	{
		if( isApplicable(info) )
		{
			event.setFunction(events.getSubmitValuesFunction("selectItemSummary"));
			event.stopProcessing();
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new SkinnySelectionProviderModel();
	}

	@NonNullByDefault(false)
	public static class SkinnySelectionProviderModel
	{
		private Boolean applicable;

		public Boolean getApplicable()
		{
			return applicable;
		}

		public void setApplicable(Boolean applicable)
		{
			this.applicable = applicable;
		}
	}
}
