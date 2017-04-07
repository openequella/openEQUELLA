package com.tle.web.sections.generic;

import java.util.Comparator;

public final class NumberOrderComparator implements Comparator<NumberOrder>
{
	public static final NumberOrderComparator HIGHEST_FIRST = new NumberOrderComparator(false);
	public static final NumberOrderComparator LOWEST_FIRST = new NumberOrderComparator(true);

	private final int orderNum;

	private NumberOrderComparator(boolean reverse)
	{
		orderNum = reverse ? -1 : 1;
	}

	@Override
	public int compare(NumberOrder o1, NumberOrder o2)
	{
		int order = o1.getOrder();
		int order2 = o2.getOrder();
		if( order == order2 )
		{
			return 0;
		}
		else
		{
			return order < order2 ? orderNum : -orderNum;
		}
	}
}
