package com.tle.web.selection.filter;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author aholland
 */
public class SelectionFilter implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Collection<String> allowedMimeTypes;

	public Collection<String> getAllowedMimeTypes()
	{
		return allowedMimeTypes;
	}

	public void setAllowedMimeTypes(Collection<String> allowedMimeTypes)
	{
		this.allowedMimeTypes = allowedMimeTypes;
	}
}