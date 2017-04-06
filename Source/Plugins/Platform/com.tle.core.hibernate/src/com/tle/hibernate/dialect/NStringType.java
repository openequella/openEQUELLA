package com.tle.hibernate.dialect;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;

@SuppressWarnings("nls")
public class NStringType extends AbstractSingleColumnStandardBasicType<String> implements DiscriminatorType<String>
{
	private static final long serialVersionUID = 1L;

	public NStringType()
	{
		super(NVarcharTypeDescriptor.INSTANCE, StringTypeDescriptor.INSTANCE);
	}

	@Override
	public String getName()
	{
		return "string";
	}

	@Override
	protected boolean registerUnderJavaType()
	{
		return true;
	}

	@Override
	public String objectToSQLString(String value, Dialect dialect)
	{
		return '\'' + value + '\'';
	}

	@Override
	public String stringToObject(String xml)
	{
		return xml;
	}

	@Override
	public String toString(String value)
	{
		return value;
	}

}
