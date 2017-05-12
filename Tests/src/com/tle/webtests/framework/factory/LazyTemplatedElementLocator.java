package com.tle.webtests.framework.factory;

import java.lang.reflect.Field;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import com.tle.webtests.pageobject.PageObject;

public class LazyTemplatedElementLocator implements ElementLocator
{
	private final PageObject pageObject;
	private SearchContext searchContext;
	private By by;
	private WebElement cachedElement;
	private TemplatedAnnotations annotations;
	private long lastLookup;
	private boolean dontCache;

	public LazyTemplatedElementLocator(PageObject pageObject, Field field)
	{
		annotations = new TemplatedAnnotations(field, pageObject);
		this.dontCache = true; // annotations.isDontCache();
		this.pageObject = pageObject;
	}

	public LazyTemplatedElementLocator(PageObject pageObject, WebElement cachedElement, SearchContext searchContext,
		By by)
	{
		this.pageObject = pageObject;
		this.searchContext = searchContext;
		this.by = by;
		this.cachedElement = cachedElement;
		this.lastLookup = System.currentTimeMillis();
	}

	public boolean isWrapChildElements()
	{
		return true;
	}

	public PageObject getPageObject()
	{
		return pageObject;
	}

	@Override
	public WebElement findElement()
	{
		if( cachedElement == null || lastLookup < pageObject.getRefreshTime() )
		{
			WebElement newElement = getSearchContext().findElement(getBy());
			if( dontCache )
			{
				return newElement;
			}
			cachedElement = newElement;
			lastLookup = System.currentTimeMillis();
		}
		return cachedElement;
	}

	public void invalidateCache()
	{
		cachedElement = null;
	}

	private SearchContext getSearchContext()
	{
		if( searchContext == null )
		{
			searchContext = pageObject.getSearchContext();
		}
		return searchContext;
	}

	private By getBy()
	{
		if( by == null )
		{
			by = annotations.buildBy();
		}
		return by;
	}

	@Override
	public List<WebElement> findElements()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString()
	{
		return "RefreshingElement located by: " + getBy() + " context:" + getSearchContext();
	}

	public WebElement findNonWrapped()
	{
		SearchContext ourSearchContext = getSearchContext();
		if( ourSearchContext instanceof RefreshableElement )
		{
			ourSearchContext = ((RefreshableElement) ourSearchContext).findNonWrapped();
		}
		return ourSearchContext.findElement(getBy());
	}
}
