/*
 * Created on Oct 26, 2005
 */
package com.tle.common;

public class Triple<FIRST, SECOND, THIRD> extends Pair<FIRST, SECOND>
{
	private static final long serialVersionUID = 1;

	private THIRD third;

	public Triple()
	{
		super();
	}

	public Triple(FIRST first, SECOND second, THIRD third)
	{
		super(first, second);
		this.third = third;
	}

	public THIRD getThird()
	{
		return third;
	}

	public void setThird(THIRD third)
	{
		this.third = third;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + third.hashCode();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean checkFields(Pair<FIRST, SECOND> rhs)
	{
		Triple<FIRST, SECOND, THIRD> t = (Triple<FIRST, SECOND, THIRD>) rhs;
		return super.checkFields(t) && Check.bothNullOrEqual(t.getThird(), getThird());
	}
}
