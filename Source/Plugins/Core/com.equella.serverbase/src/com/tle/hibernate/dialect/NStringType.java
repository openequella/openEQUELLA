/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
