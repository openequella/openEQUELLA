package com.tle.hibernate.dialect;

import java.sql.Types;

import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class NVarcharTypeDescriptor extends VarcharTypeDescriptor
{
	private static final long serialVersionUID = 1L;
	public static final NVarcharTypeDescriptor INSTANCE = new NVarcharTypeDescriptor();

	@Override
	public int getSqlType()
	{
		return Types.NVARCHAR;
	}
}
