package com.tle.core.imagemagick;

import com.dytech.edge.exceptions.RuntimeApplicationException;

/**
 * It's runtime, so you don't have to catch it! Bonus!
 * 
 * @author Aaron
 */
public class ImageMagickException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	public ImageMagickException(String message)
	{
		super(message);
		setLogged(true);
	}

	public ImageMagickException(String message, Throwable t)
	{
		super(message, t);
		setLogged(true);
	}
}