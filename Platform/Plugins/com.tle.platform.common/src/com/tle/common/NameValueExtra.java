/**
 * 
 */
package com.tle.common;

/**
 * Rather than make the NameValue parent a Comparable and risk unintended
 * consequences in any of the many places where that class is utilised, we'll
 * adopt this class as our comparable-by-name NameValue variant.
 * 
 * @author larry
 */
public class NameValueExtra extends NameValue implements Comparable<NameValueExtra>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6675973016715913685L;

	private String extra;

	public NameValueExtra(String name, String value, String extra)
	{
		super(name, value);
		this.extra = extra;
	}

	public String getExtra()
	{
		return extra;
	}

	@Override
	public boolean checkFields(Pair<String, String> rhs)
	{
		NameValueExtra t = (NameValueExtra) rhs;
		return super.checkFields(t) && Check.bothNullOrEqual(t.getExtra(), getExtra());
	}

	/**
	 * compare name, value, extra in that order until we get a non-0 result
	 */
	@Override
	public int compareTo(NameValueExtra o)
	{
		int strCompare = 0;

		if( o == null )
		{
			return 1;
		}
		if( this.getFirst() == null )
		{
			return -1;
		}
		else
		{
			strCompare = this.getFirst().compareToIgnoreCase(o.getFirst());
			if( strCompare == 0 && this.getSecond() != null )
			{
				strCompare = this.getSecond().compareToIgnoreCase(o.getSecond());
				if( strCompare == 0 && this.getExtra() != null )
				{
					strCompare = this.getExtra().compareToIgnoreCase(o.getExtra());
				}
			}
		}
		return strCompare;
	}
}
