package com.tle.hibernate.dialect;

import java.sql.Types;

import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class LongNVarcharTypeDescriptor extends VarcharTypeDescriptor
{
	private static final long serialVersionUID = 1L;
	public static final LongNVarcharTypeDescriptor INSTANCE = new LongNVarcharTypeDescriptor();

	@Override
	public int getSqlType()
	{
		return Types.LONGNVARCHAR;
	}
}
