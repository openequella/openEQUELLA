package com.tle.core.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface MapperExtension
{
	void extendMapper(ObjectMapper mapper);
}
