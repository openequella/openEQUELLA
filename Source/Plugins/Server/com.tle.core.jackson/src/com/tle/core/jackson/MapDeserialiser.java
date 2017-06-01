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

package com.tle.core.jackson;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Takes a JSON string and turns into a Map of objects. Or a Map of a Map of
 * objects, as the case may be.
 * 
 * @author Aaron
 */
public class MapDeserialiser extends StdDeserializer<Map<String, Object>>
{
	public MapDeserialiser()
	{
		super(Map.class);
	}

	@Override
	public Map<String, Object> deserialize(JsonParser parser, DeserializationContext context) throws IOException,
		JsonProcessingException
	{
		final Map<String, Object> map = Maps.newHashMap();
		int times = 0;
		while( parser.hasCurrentToken() )
		{
			final JsonToken currentToken = parser.getCurrentToken();
			switch( currentToken )
			{
				case START_OBJECT:
					// sure
					times++;
					if( times > 1 )
					{
						throw context.mappingException("Object opened twice??");
					}
					break;

				case END_OBJECT:
					return map;

				case FIELD_NAME:
					parser.nextToken();
					map.put(parser.getCurrentName(), parse(parser, context));
					break;

				case NOT_AVAILABLE:
					parser.nextToken();
					break;

				default:
					throw context.mappingException("Object can only contain fields " + where(parser, context));
			}
			parser.nextToken();
		}
		throw context.mappingException("No end object encountered " + where(parser, context));
	}

	private static String where(JsonParser parser, DeserializationContext context)
	{
		return "";
	}

	private Object parse(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException
	{
		final JsonToken currentToken = parser.getCurrentToken();
		switch( currentToken )
		{
			case START_OBJECT:
				final Map<String, Object> oe = new MapDeserialiser().deserialize(parser, context);
				if( parser.getCurrentToken() != JsonToken.END_OBJECT )
				{
					throw context.mappingException("Should be on end object " + where(parser, context));
				}
				return oe;

			case VALUE_FALSE:
			case VALUE_TRUE:
				return parser.getValueAsBoolean();

			case VALUE_NUMBER_INT:
				return parser.getValueAsInt();

			case VALUE_NUMBER_FLOAT:
				return parser.getValueAsDouble();

			case VALUE_NULL:
				return "null";

			case VALUE_STRING:
				return "\"" + parser.getText() + "\"";

			case START_ARRAY:
				final Object[] ae = parseArray(parser, context);
				if( parser.getCurrentToken() != JsonToken.END_ARRAY )
				{
					throw context.mappingException("Should be on end array " + where(parser, context));
				}
				return ae;

			case END_ARRAY:
			case END_OBJECT:
				// whoops
				throw context.mappingException("Unexpected end object/array " + where(parser, context));

			case FIELD_NAME:
			case NOT_AVAILABLE:
				throw context.mappingException("How? " + where(parser, context));

			default:
				throw context.mappingException("Unhandled token " + where(parser, context));
		}
	}

	private Object[] parseArray(JsonParser parser, DeserializationContext context) throws IOException,
		JsonProcessingException
	{
		boolean started = false;
		final List<Object> values = Lists.newArrayList();
		while( parser.hasCurrentToken() )
		{
			final JsonToken currentToken = parser.getCurrentToken();
			switch( currentToken )
			{
				case START_ARRAY:
					if( started )
					{
						values.add(parseArray(parser, context));
					}
					started = true;
					break;
				case END_ARRAY:
					if( !started )
					{
						throw context.mappingException("End array encountered without start " + where(parser, context));
					}
					return values.toArray();
				case NOT_AVAILABLE:
					break;
				default:
					if( !started )
					{
						throw context.mappingException("Value encountered without array start "
							+ where(parser, context));
					}
					values.add(parse(parser, context));
			}
			parser.nextToken();
		}
		throw context.mappingException("No end array found " + where(parser, context));
	}
}
