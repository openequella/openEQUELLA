package com.tle.common.workflow;

public enum Trend
{
	WEEK(7), MONTH(30);

	private final int days;

	Trend(int days)
	{
		this.days = days;
	}

	public int getDays()
	{
		return days;
	}
}
