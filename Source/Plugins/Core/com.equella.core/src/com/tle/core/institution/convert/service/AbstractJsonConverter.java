package com.tle.core.institution.convert.service;

import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.JsonHelper;

import javax.inject.Inject;

public abstract class AbstractJsonConverter<T> extends AbstractConverter<T>
{
	@Inject
	protected JsonHelper json;
}
