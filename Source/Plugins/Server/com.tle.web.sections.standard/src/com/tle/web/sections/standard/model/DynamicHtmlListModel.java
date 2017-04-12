package com.tle.web.sections.standard.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public abstract class DynamicHtmlListModel<T> implements HtmlListModel<T>
{
	private boolean sort;
	@Nullable
	private String defaultValue;
	private Comparator<? super Option<T>> comparator = OptionNameComparator.INSTANCE;

	public void setSort(boolean sort)
	{
		this.sort = sort;
	}

	@Override
	public List<Option<T>> getOptions(SectionInfo info)
	{
		ListState<T> listState = getListState(info);
		if( !listState.sorted )
		{
			listState.sorted = true;
			listState.options = sort(listState.options);
		}
		return listState.options;
	}

	private List<Option<T>> sort(List<Option<T>> options)
	{
		if( sort && options.size() > 1 )
		{
			List<Option<T>> sortThis = options;
			if( getTopOption() != null )
			{
				sortThis = options.subList(1, options.size());
			}
			Collections.sort(sortThis, comparator);
		}
		return options;
	}

	@Nullable
	@Override
	public String getDefaultValue(SectionInfo info)
	{
		if( defaultValue != null )
		{
			return defaultValue;
		}
		List<Option<T>> options = getOptions(info);
		if( !options.isEmpty() )
		{
			return options.get(0).getValue();
		}
		return null;
	}

	@Nullable
	@Override
	public Option<T> getOption(SectionInfo info, String value)
	{
		return getListState(info).modelMap.get(value);
	}

	@Override
	public Set<String> getMatchingValues(SectionInfo info, Set<String> values)
	{
		Set<String> keySet = getListState(info).modelMap.keySet();
		if( keySet.containsAll(values) )
		{
			return values;
		}
		Set<String> matching = new HashSet<String>();
		for( String val : values )
		{
			if( keySet.contains(val) )
			{
				matching.add(val);
			}
		}
		return matching;
	}

	@Nullable
	@Override
	public T getValue(SectionInfo info, String value)
	{
		Option<T> option = getOption(info, value);
		if( option == null )
		{
			return null;
		}
		return option.getObject();
	}

	@Override
	public List<T> getValues(SectionInfo info, Collection<String> values)
	{
		List<T> vals = new ArrayList<T>();
		for( String value : values )
		{
			T val = getValue(info, value);
			if( val != null )
			{
				vals.add(val);
			}
		}
		return vals;
	}

	private ListState<T> getListState(SectionInfo info)
	{
		ListState<T> listState = info.getAttribute(this);
		if( listState == null )
		{
			listState = new ListState<T>();
			Option<T> topOpt = getTopOption();
			if( topOpt != null )
			{
				addOption(topOpt, listState);
			}
			Iterable<T> values = populateModel(info);
			if( values != null )
			{
				for( T val : values )
				{
					Option<T> opt = convertToOption(info, val);
					addOption(opt, listState);
				}
			}
			Iterable<Option<T>> options = populateOptions(info);
			if( options != null )
			{
				for( Option<T> option : options )
				{
					addOption(option, listState);
				}
			}
			info.setAttribute(this, listState);
		}
		return listState;
	}

	@Nullable
	protected Iterable<Option<T>> populateOptions(SectionInfo info)
	{
		return null;
	}

	@Nullable
	protected Option<T> getTopOption()
	{
		return null;
	}

	private void addOption(Option<T> opt, ListState<T> listState)
	{
		listState.options.add(opt);
		listState.modelMap.put(opt.getValue(), opt);
	}

	protected Option<T> convertToOption(SectionInfo info, T obj)
	{
		return SimpleHtmlListModel.defaultConvertToOption(obj);
	}

	@Nullable
	protected abstract Iterable<T> populateModel(SectionInfo info);

	protected static class ListState<T>
	{
		boolean sorted;
		List<Option<T>> options = new ArrayList<Option<T>>();
		Map<String, Option<T>> modelMap = new HashMap<String, Option<T>>();
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	@Override
	public String getStringValue(SectionInfo info, T value)
	{
		return convertToOption(info, value).getValue();
	}

	public void setComparator(Comparator<? super Option<T>> comparator)
	{
		this.comparator = comparator;
	}

	public Comparator<? super Option<T>> getComparator()
	{
		return comparator;
	}
}
