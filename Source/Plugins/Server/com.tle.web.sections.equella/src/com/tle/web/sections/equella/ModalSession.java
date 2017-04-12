package com.tle.web.sections.equella;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class ModalSession implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final String KEY_IGNORE_CURRENT_SESSION = "KEY_IGNORE_CURRENT_SESSION";

	private final ModalSessionCallback finishedCallback;
	private Map<Object, Object> attributes;

	public ModalSession(ModalSessionCallback finishedCallback)
	{
		this.finishedCallback = finishedCallback;
	}

	public ModalSessionCallback getFinishedCallback()
	{
		return finishedCallback;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Object key)
	{
		if( attributes == null )
		{
			return null;
		}
		return (T) attributes.get(key);
	}

	public void setAttribute(Object key, Object attribute)
	{
		if( attributes == null )
		{
			attributes = new HashMap<Object, Object>();
		}
		attributes.put(key, attribute);
	}
}
