/*
 * Created on 4/05/2006
 */
package com.tle.core.institution.convert;

import com.tle.common.NameValue;

public abstract class AbstractConverter<T> extends AbstractMigratableConverter<T>
{
	public abstract ConverterId getConverterId();

	@Override
	public String getStringId()
	{
		ConverterId converterId = getConverterId();
		if( converterId != null )
		{
			return converterId.name();
		}
		return null;
	}

	protected NameValue getStandardTask(ConverterId converterId)
	{
		return getStandardTask(converterId.name());
	}

	public static class FormatFile
	{
		private boolean bucketed;

		public boolean isBucketed()
		{
			return bucketed;
		}

		public void setBucketed(boolean bucketed)
		{
			this.bucketed = bucketed;
		}
	}
}
