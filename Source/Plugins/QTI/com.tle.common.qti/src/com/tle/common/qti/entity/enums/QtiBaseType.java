package com.tle.common.qti.entity.enums;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public enum QtiBaseType
{
	IDENTIFIER, BOOLEAN, INTEGER, FLOAT, STRING, POINT, PAIR, DIRECTED_PAIR
	{
		@Override
		public String toString()
		{
			return "directedPair";
		}
	},
	DURATION, FILE, URI, INT_OR_IDENTIFIER
	{
		@Override
		public String toString()
		{
			return "intOrIdentifier";
		}
	};

	@Override
	public String toString()
	{
		return name().toLowerCase();
	}

	@Nullable
	public static QtiBaseType fromString(@Nullable String name)
	{
		if( name == null )
		{
			return null;
		}
		if( name.equals("directedPair") )
		{
			return DIRECTED_PAIR;
		}
		if( name.equals("intOrIdentifier") )
		{
			return INT_OR_IDENTIFIER;
		}
		return QtiBaseType.valueOf(name.toUpperCase());
	}
}
