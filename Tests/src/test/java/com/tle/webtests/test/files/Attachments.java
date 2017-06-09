package com.tle.webtests.test.files;

import java.net.URL;

public class Attachments
{
	public static URL get(String file)
	{
		return Attachments.class.getResource(file);
	}
}
