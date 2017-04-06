package com.tle.core.search;

/**
 * A wrapper class for the HierarchyTopic/DynamicCollection, with extra fields
 * to contain a virtualised value string, and a counter. For dynamicCollections,
 * the counter is not initially set (and may not be set at all). For
 * HierarchyTopic the tally is set, and is displayed when navigating into
 * sub-hierarchies
 * 
 * @author larry
 */
public class VirtualisableAndValue<T>
{

	private T vt;

	private String virtualisedValue;

	private int count;

	public VirtualisableAndValue(T ht)
	{
		this.vt = ht;
	}

	public VirtualisableAndValue(T ht, String virtualisedValue, int count)
	{
		this(ht);
		this.virtualisedValue = virtualisedValue;
		this.count = count;
	}

	public T getVt()
	{
		return vt;
	}

	public void setVt(T ht)
	{
		this.vt = ht;
	}

	public String getVirtualisedValue()
	{
		return virtualisedValue;
	}

	public void setVirtualisedValue(String virtualisedValue)
	{
		this.virtualisedValue = virtualisedValue;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}
}
