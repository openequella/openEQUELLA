package com.tle.json;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BaseJSONCreator
{
	protected final static ObjectMapper mapper = new ObjectMapper();

	public static ObjectNode parse(String json)
	{
		try
		{
			return (ObjectNode) mapper.readTree(json);
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
	}
}
