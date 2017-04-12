package com.tle.web.pss.itemlist;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.pss.entity.PssCallbackLog;
import com.tle.core.pss.service.PearsonScormServicesCallbackService;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;

@Bind
public class PearsonScormServicesNotificationListEntry extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, ItemListEntry>
{
	@PlugKey("notification.message")
	private static Label MESSAGE_LABEL;

	@Inject
	private PearsonScormServicesCallbackService pssCallbackService;

	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(RenderContext context, List<ItemListEntry> entries,
		ListSettings<ItemListEntry> listSettings)
	{
		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@Override
			public void processEntry(ItemListEntry entry)
			{
				PssCallbackLog logEntry = pssCallbackService.getCallbackLogEntry(entry.getItem());
				if( logEntry != null && Check.isEmpty(logEntry.getMessage()) )
				{
					entry.addDelimitedMetadata(MESSAGE_LABEL, logEntry.getMessage());
				}
			}
		};
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}
