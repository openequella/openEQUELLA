package com.tle.webtests.framework.factory;

import java.lang.reflect.Field;

import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

import com.tle.webtests.pageobject.PageObject;

public class LazyTemplatedElementLocatorFactory implements ElementLocatorFactory
{
	private PageObject pageObject;

	public LazyTemplatedElementLocatorFactory(PageObject pageObject)
	{
		this.pageObject = pageObject;
	}

	@Override
	public ElementLocator createLocator(Field field)
	{
		return new LazyTemplatedElementLocator(pageObject, field);
	}

}
