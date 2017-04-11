/*
 * Created on Mar 10, 2005
 */
package com.dytech.common;

/**
 * @author Nicholas Read
 */
public final class GeneralConstants
{
	public static final String ISO_DATE_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ss"; //$NON-NLS-1$

	// /////////////////////////////////////////////////////////////////////////
	// Binary Sizes
	//
	// You say kilobyte, megabyte; I say kibibyte, mebibyte
	// @see https://en.wikipedia.org/wiki/Kilobyte
	// However, seeing as we're imposing a restriction on the user(s), it seems
	// best to be
	// conservatively generous by using the higher numbers hence 1024(pow n)
	// rather than 1,000(pow n)

	public static final int BYTES_PER_KILOBYTE = 1024;
	public static final int KILOBYTES_PER_MEGABYTE = 1024;

	public static final int BYTES_PER_MEGABYTE = BYTES_PER_KILOBYTE * KILOBYTES_PER_MEGABYTE;

	public static final long GIGABYTE = BYTES_PER_MEGABYTE * 1024;

	// a placeholder for counters etc which would be >= 0 if actually determined
	public static final int UNCALCULATED = -1;

	// /////////////////////////////////////////////////////////////////////////
	// Private methods

	/**
	 * Do not contruct this object.
	 */
	private GeneralConstants()
	{
		throw new Error();
	}
}