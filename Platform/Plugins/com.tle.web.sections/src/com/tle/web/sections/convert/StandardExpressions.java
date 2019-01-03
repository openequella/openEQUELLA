/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.convert;

import java.util.Collection;

import net.entropysoft.transmorph.ConversionContext;
import net.entropysoft.transmorph.ConverterException;
import net.entropysoft.transmorph.IContainerConverter;
import net.entropysoft.transmorph.IConverter;
import net.entropysoft.transmorph.type.TypeReference;

import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.expression.LabelExpression;
import com.tle.web.sections.js.generic.expression.NullExpression;
import com.tle.web.sections.js.generic.expression.PrimitiveValueExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.AssignableFunction;
import com.tle.web.sections.render.Label;

public class StandardExpressions implements IContainerConverter
{
	private IConverter stringConverter;
	private IConverter elementConverter;

	public StandardExpressions(IConverter stringConverter)
	{
		this.stringConverter = stringConverter;
	}

	@Override
	public boolean canHandle(ConversionContext context, Object sourceObject, TypeReference<?> destinationType)
	{
		return (destinationType.getRawType() == JSExpression.class);
	}

	@Override
	public Object convert(ConversionContext context, Object obj, TypeReference<?> destinationType)
		throws ConverterException
	{
		if( obj == null )
		{
			return new NullExpression();
		}
		else if( obj instanceof JSExpression )
		{
			return obj;
		}
		else if( obj instanceof String )
		{
			return new StringExpression((String) obj);
		}
		else if( obj instanceof Boolean )
		{
			return new PrimitiveValueExpression((Boolean) obj);
		}
		else if( obj instanceof Integer )
		{
			return new PrimitiveValueExpression((Integer) obj);
		}
		else if( obj instanceof Long )
		{
			return new PrimitiveValueExpression((Long) obj);
		}
		else if( obj instanceof Double )
		{
			return new PrimitiveValueExpression((Double) obj);
		}
		else if( obj instanceof Float )
		{
			return new PrimitiveValueExpression((Float) obj);
		}
		else if( obj instanceof ElementId )
		{
			return new ElementByIdExpression((ElementId) obj);
		}
		else if( obj instanceof JSCallable )
		{
			return AssignableFunction.get((JSCallable) obj);
		}
		else if( obj instanceof Label )
		{
			return new LabelExpression(((Label) obj));
		}
		else if( obj instanceof JSExpression[] )
		{
			return new ArrayExpression((JSExpression[]) obj);
		}
		else if( obj.getClass().isArray() || obj instanceof Collection<?> )
		{
			return new ArrayExpression((JSExpression[]) elementConverter.convert(context, obj,
				TypeReference.get(JSExpression[].class)));
		}
		else if( obj.getClass().isEnum() )
		{
			return new StringExpression(((Enum<?>) obj).name());
		}
		return new StringExpression((String) stringConverter.convert(context, obj, TypeReference.get(String.class)));
	}

	@Override
	public IConverter getElementConverter()
	{
		return elementConverter;
	}

	@Override
	public void setElementConverter(IConverter elementConverter)
	{
		this.elementConverter = elementConverter;
	}
}
