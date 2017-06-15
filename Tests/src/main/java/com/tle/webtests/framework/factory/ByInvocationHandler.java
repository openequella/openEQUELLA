package com.tle.webtests.framework.factory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.openqa.selenium.support.FindBy;

import com.google.common.base.Throwables;
import com.tle.webtests.pageobject.PageObject;

public class ByInvocationHandler implements InvocationHandler
{
	private PageObject page;
	private FindBy findBy;

	public ByInvocationHandler(PageObject page, FindBy findBy)
	{
		this.findBy = findBy;
		this.page = page;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		Object result = method.invoke(findBy);
		if( result instanceof String )
		{
			String using = (String) result;
			if( using.isEmpty() )
			{
				return using;
			}
			// parse the using, and replace {xx} with method calls on the page
			// object
			final StringBuilder use = new StringBuilder();
			final char[] text = using.toCharArray();
			final Class<?> pageClass = page.getClass();
			boolean inside = false;
			boolean firstInside = false;
			StringBuilder insideText = new StringBuilder();

			for( int i = 0; i < text.length; i++ )
			{
				char c = text[i];
				if( !inside && c == '{' )
				{
					inside = true;
					firstInside = true;
				}
				else if( inside && c == '}' )
				{
					inside = false;
					try
					{
						final String methodName = "get" + insideText.toString();

						Method m = findMethod(pageClass, methodName);
						m.setAccessible(true);
						use.append(m.invoke(page));
					}
					catch( Exception e )
					{
						throw Throwables.propagate(e);
					}
					insideText = new StringBuilder();
				}
				else if( inside )
				{
					if( firstInside )
					{
						insideText.append(Character.toUpperCase(c));
					}
					else
					{
						insideText.append(c);
					}
					firstInside = false;
				}
				else
				{
					use.append(c);
				}
			}
			return use.toString();
		}
		return result;
	}

	private Method findMethod(Class<?> c, String methodName) throws NoSuchMethodException
	{
		Method m;
		Class<?> c2 = c;
		while( c2 != Object.class )
		{
			try
			{
				m = c2.getDeclaredMethod(methodName);
				return m;
			}
			catch( NoSuchMethodException nsm )
			{
				c2 = c2.getSuperclass();
			}
		}
		throw new NoSuchMethodException(methodName);
	}
}
