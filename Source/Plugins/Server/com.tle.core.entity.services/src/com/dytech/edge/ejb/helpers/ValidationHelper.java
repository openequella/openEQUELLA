package com.dytech.edge.ejb.helpers;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.Logger;

import com.dytech.common.xml.ReflectionProvider;
import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author nread
 */
public final class ValidationHelper
{
	private static final ReflectionProvider REFLECTION = new ReflectionProvider();
	private static final Logger LOGGER = Logger.getLogger(ValidationHelper.class);

	private ValidationHelper()
	{
		throw new Error("Do not invoke");
	}

	public static boolean checkBlankFields(Object entity, String[] blanks, List<ValidationError> errors)
	{
		boolean foundErrors = false;
		for( String field : blanks )
		{
			Object value = null;
			try
			{
				value = REFLECTION.getField(entity, field);
			}
			catch( Exception ex )
			{
				String getter = "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1); //$NON-NLS-1$
				try
				{
					Method method = entity.getClass().getMethod(getter, new Class[]{});
					value = method.invoke(entity, new Object[0]);
				}
				catch( Exception ex2 )
				{
					LOGGER.error("Could not access field '" + field + "'", ex); //$NON-NLS-1$//$NON-NLS-2$
					LOGGER.error("Could not invoke getter method '" + getter + "()'", ex2); //$NON-NLS-1$ //$NON-NLS-2$

					throw new RuntimeApplicationException("Error validating entity for field/getter method '" + field
						+ "'");
				}
			}

			if( value == null || value.toString().trim().length() == 0 )
			{
				foundErrors = true;
				errors.add(new ValidationError(field, CurrentLocale
					.get("com.tle.core.services.entity.generic.validation.fillallmandatory"))); //$NON-NLS-1$
			}
		}
		return foundErrors;
	}
}
