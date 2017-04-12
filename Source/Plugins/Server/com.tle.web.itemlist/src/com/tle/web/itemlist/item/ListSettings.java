package com.tle.web.itemlist.item;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tle.web.itemlist.ListEntry;

public class ListSettings<T extends ListEntry>
{
	private boolean editable = true;
	private Collection<String> hilightedWords;
	private List<T> entries;
	private Map<Object, Object> attrs = Maps.newHashMap();

	public Collection<String> getHilightedWords()
	{
		return hilightedWords;
	}

	public void setHilightedWords(Collection<String> hilightedWords)
	{
		this.hilightedWords = hilightedWords;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public List<T> getEntries()
	{
		return entries;
	}

	public void setEntries(List<T> entries)
	{
		this.entries = entries;
	}

	@SuppressWarnings("unchecked")
	public <A> A getAttribute(Object key)
	{
		return (A) attrs.get(key);
	}

	public void setAttribute(Object key, Object value)
	{
		attrs.put(key, value);
	}

}
