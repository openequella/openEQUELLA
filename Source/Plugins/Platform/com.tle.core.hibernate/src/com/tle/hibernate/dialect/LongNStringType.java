package com.tle.hibernate.dialect;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;

@SuppressWarnings("nls")
public class LongNStringType extends AbstractSingleColumnStandardBasicType<String>
{
	private static final long serialVersionUID = 1L;

	public LongNStringType()
	{
		super(LongNVarcharTypeDescriptor.INSTANCE, StringTypeDescriptor.INSTANCE);
	}

	@Override
	public String getName()
	{
		return "materialized_clob";
	}
}
