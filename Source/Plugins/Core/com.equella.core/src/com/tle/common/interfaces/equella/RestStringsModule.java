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

package com.tle.common.interfaces.equella;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Maps;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.interfaces.SimpleI18NString;

/**
 */
public class RestStringsModule extends SimpleModule
{
	public RestStringsModule()
	{
		super("RestStringsModule", Version.unknownVersion());
		addSerializer(I18NString.class, new StringSerializer());
		addSerializer(I18NStrings.class, new StringsSerializer());
		addAbstractTypeMapping(I18NString.class, SimpleI18NString.class);
		addDeserializer(SimpleI18NString.class, new StringDeserializer());
		addDeserializer(I18NStrings.class, new StringsDeserializer());
	}

	public static class StringDeserializer extends JsonDeserializer<SimpleI18NString>
	{
		@Override
		public SimpleI18NString deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
			JsonProcessingException
		{
			JsonToken currentToken = jp.getCurrentToken();
			if( currentToken != JsonToken.VALUE_STRING )
			{
				throw new JsonParseException("Must be a string", jp.getCurrentLocation());
			}
			String text = jp.getText();
			return new SimpleI18NString(text);
		}
	}

	public static class StringsDeserializer extends JsonDeserializer<I18NStrings>
	{
		@Override
		public I18NStrings deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
			JsonProcessingException
		{
			Map<String, LanguageString> strings = Maps.newHashMap();
			JsonToken currentToken = jp.getCurrentToken();
			if( currentToken != JsonToken.START_OBJECT )
			{
				throw new JsonParseException("Must be an object", jp.getCurrentLocation());
			}
			while( jp.nextToken() == JsonToken.FIELD_NAME )
			{
				String textValue = jp.nextTextValue();
				LanguageBundle tempBundle = LangUtils.createTempLangugageBundle(null, textValue);
				LanguageString tempLangString = LangUtils.createLanguageString(tempBundle, CurrentLocale.getLocale(),
					textValue);
				strings.put(jp.getCurrentName(), tempLangString);
			}
			return new SimpleI18NStrings(strings);
		}
	}

	public static class StringSerializer extends JsonSerializer<I18NString>
	{
		@Override
		public void serialize(I18NString value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException
		{
			jgen.writeString(value.toString());
		}
	}

	public static class StringsSerializer extends JsonSerializer<I18NStrings>
	{

		@Override
		public void serialize(I18NStrings value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException
		{
			jgen.writeStartObject();
			Map<String, String> strings = value.getStrings();
			for( Entry<String, String> string : strings.entrySet() )
			{
				jgen.writeFieldName(string.getKey());
				jgen.writeString(string.getValue());
			}
			jgen.writeEndObject();
		}
	}
}
