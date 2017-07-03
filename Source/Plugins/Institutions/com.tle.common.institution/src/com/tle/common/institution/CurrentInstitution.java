package com.tle.common.institution;

import com.tle.beans.Institution;

/**
 * @author Nicholas Read
 */
public final class CurrentInstitution
{
	private static final ThreadLocal<Institution> local = new ThreadLocal<Institution>();

	public static Institution get()
	{
		return local.get();
	}

	public static void set(Institution institution)
	{
		local.set(institution);
	}

	public static void remove()
	{
		local.remove();
	}

	private CurrentInstitution()
	{
		throw new Error();
	}
}
