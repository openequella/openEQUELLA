package com.tle.core.institution;

import java.io.Serializable;

import com.tle.common.i18n.InternalI18NString;

public class InstitutionValidationError implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String id;
	private final InternalI18NString message;

	public InstitutionValidationError(String id, InternalI18NString message)
	{
		this.id = id;
		this.message = message;
	}

	public String getId()
	{
		return id;
	}

	public InternalI18NString getMessage()
	{
		return message;
	}
}
