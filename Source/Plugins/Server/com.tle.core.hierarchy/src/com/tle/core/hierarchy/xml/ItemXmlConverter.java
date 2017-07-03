package com.tle.core.hierarchy.xml;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.core.item.service.ItemService;

public class ItemXmlConverter implements Converter
{
	private final ItemService itemService;

	public ItemXmlConverter(ItemService itemService)
	{
		this.itemService = itemService;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean canConvert(Class clazz)
	{
		return Item.class.isAssignableFrom(clazz);
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		Item item = (Item) obj;
		writer.addAttribute("itemid", item.getItemId().toString()); //$NON-NLS-1$
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		ItemId id = new ItemId(reader.getAttribute("itemid")); //$NON-NLS-1$
		try
		{
			return itemService.get(id);
		}
		catch( NotFoundException nfe )
		{
			return null;
		}
	}

}
