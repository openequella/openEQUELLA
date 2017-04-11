package com.dytech.common.io;

import java.util.regex.Pattern;

class ZipFilter
{
	private Pattern p;

	public ZipFilter(String regex)
	{
		p = Pattern.compile(regex);
	}

	public boolean accept(String filename)
	{
		return p.matcher(filename).matches();
	}
}
