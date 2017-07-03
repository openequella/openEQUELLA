package com.tle.core.institution.convert;

import java.io.Serializable;

import com.dytech.edge.common.Constants;
import com.tle.common.beans.progress.MessageCallback;
import com.tle.common.i18n.CurrentLocale;

public class DefaultMessageCallback implements MessageCallback, Serializable
{
	private static final long serialVersionUID = 1L;

	private int current;
	private long total;
	private String key;
	private Object[] values = {0, Constants.BLANK, Constants.BLANK};

	public DefaultMessageCallback(String key)
	{
		this.key = key;
	}

	public void setType(String type)
	{
		values[2] = type;
	}

	public int getCurrent()
	{
		return current;
	}

	public void setCurrent(int current)
	{
		this.current = current;
		values[0] = current;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public long getTotal()
	{
		return total;
	}

	public void setTotal(long total)
	{
		this.total = total;
		values[1] = total;
	}

	public Object[] getValues()
	{
		return values;
	}

	public void setValues(Object[] values)
	{
		this.values = values;
	}

	@Override
	public String getMessage()
	{
		if( key != null )
		{
			return CurrentLocale.get(key, values);
		}
		return null;
	}

	public void incrementCurrent()
	{
		current++;
		values[0] = current;
	}
}
