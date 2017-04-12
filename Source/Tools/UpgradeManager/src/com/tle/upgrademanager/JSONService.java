package com.tle.upgrademanager;

import com.google.gson.Gson;

public class JSONService
{
	private static Gson gson = new Gson();

	public static String toString(Object object)
	{
		return gson.toJson(object);
	}

}
