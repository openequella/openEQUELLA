package com.tle.core.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.annotation.NonNullByDefault;

@NonNullByDefault
public interface ObjectMapperService
{
	ObjectMapper createObjectMapper(String... named);
}
