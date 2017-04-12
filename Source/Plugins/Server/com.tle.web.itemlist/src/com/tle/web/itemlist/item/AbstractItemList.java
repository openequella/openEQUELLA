package com.tle.web.itemlist.item;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension.ProcessEntryCallback;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.events.RenderContext;

/**
 * @author Aaron
 */
@TreeIndexed
public abstract class AbstractItemList<LE extends AbstractItemListEntry, M extends AbstractItemList.Model<LE>>
	extends
		AbstractItemlikeList<Item, LE, M> implements ItemList<LE>
{
	private static final Logger LOGGER = Logger.getLogger(AbstractItemList.class);

	@Inject
	private StandardItemListEntryFactory factory;

	private List<ItemlikeListEntryExtension<Item, LE>> extensions;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		extensions = factory.register(getExtensionTypes(), tree, id);
	}

	@SuppressWarnings("nls")
	@Override
	protected void customiseListEntries(RenderContext context, List<LE> entries)
	{
		if( Check.isEmpty(entries) )
		{
			return;
		}
		ListSettings<LE> settings = getModel(context).getListSettings();
		for( ItemlikeListEntryExtension<Item, LE> ext : getExtensions() )
		{
			ProcessEntryCallback<Item, LE> cb = ext.processEntries(context, entries, settings);
			if( cb != null )
			{
				for( LE entry : entries )
				{
					try
					{
						cb.processEntry(entry);
					}
					catch( Exception t )
					{
						LOGGER.error("Error processing list entry for " + entry.getItem().getItemId(), t);
					}
				}
			}
		}
	}

	protected List<ItemlikeListEntryExtension<Item, LE>> getExtensions()
	{
		return extensions;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model<LE>();
	}

	public static class Model<LE extends AbstractItemListEntry> extends AbstractItemlikeList.Model<Item, LE>
	{
		// Empty
	}
}
