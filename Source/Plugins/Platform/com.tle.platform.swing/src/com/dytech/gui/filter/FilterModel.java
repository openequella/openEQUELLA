package com.dytech.gui.filter;

import java.util.List;

/**
 * @author Nicholas Read
 */
public abstract class FilterModel<T>
{
	private List<T> exclusion;

	public FilterModel()
	{
		super();
	}

	public abstract List<T> search(String pattern);

	public List<T> removeExclusions(List<T> c)
	{
		if( exclusion != null && exclusion.size() > 0 )
		{
			c.removeAll(exclusion);
		}
		return c;
	}

	/**
	 * @return Returns the exclusion.
	 */
	public List<T> getExclusion()
	{
		return exclusion;
	}

	/**
	 * @param exclusion The exclusion to set.
	 */
	public void setExclusion(List<T> exclusion)
	{
		this.exclusion = exclusion;
	}
}