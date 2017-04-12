package com.tle.core.institution;

import java.util.List;

public class InstitutionValidationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private final List<InstitutionValidationError> errors;

	public InstitutionValidationException(List<InstitutionValidationError> errors)
	{
		this.errors = errors;
	}

	public List<InstitutionValidationError> getErrors()
	{
		return errors;
	}

	@SuppressWarnings("nls")
	@Override
	public String getMessage()
	{
		StringBuilder sbuf = new StringBuilder();
		boolean first = true;
		for( InstitutionValidationError error : errors )
		{
			if( !first )
			{
				sbuf.append(", ");
			}
			first = false;
			sbuf.append(error.getMessage().toString());
		}
		return "Validation errors: " + sbuf;
	}
}
