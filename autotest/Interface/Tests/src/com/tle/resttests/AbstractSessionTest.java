package com.tle.resttests;

import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;

public class AbstractSessionTest extends AbstractTest
{
	@BeforeMethod
	public void setupSubcontext(Method method)
	{
		context.setSubPrefix(method.getName());
	}
}
