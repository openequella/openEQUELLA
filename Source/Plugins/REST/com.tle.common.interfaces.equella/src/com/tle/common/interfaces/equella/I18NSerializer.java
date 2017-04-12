package com.tle.common.interfaces.equella;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.tle.common.interfaces.I18NString;

public class I18NSerializer extends StdScalarSerializer<I18NString>
{
	public I18NSerializer()
	{
		super(I18NString.class);
	}

	@Override
	public void serialize(I18NString value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
		JsonGenerationException
	{
		jgen.writeString(value.toString());
	}
}
