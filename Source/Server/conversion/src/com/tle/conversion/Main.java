package com.tle.conversion;

import java.io.File;
import java.io.IOException;

public class Main
{
	private static Converter exporter = new Converter();

	public static void main(String[] args)
	{
		try
		{
			exporter = new Converter();
			convert(new File(args[0]), new File(args[1]));
		}
		catch( Exception th )
		{
			th.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.remoting.RemoteConversionService#convert(java.io.File,
	 * java.io.File)
	 */
	public static void convert(File from, File to) throws RuntimeException
	{
		String fromPath = from.getAbsolutePath();
		String toPath = to.getAbsolutePath();
		try
		{
			exporter.exportFile(fromPath, toPath);
		}
		catch( IOException ex )
		{
			throw new RuntimeException("Error", ex);
		}
	}
}
