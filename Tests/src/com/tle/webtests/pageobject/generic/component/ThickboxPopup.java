package com.tle.webtests.pageobject.generic.component;

import com.tle.webtests.pageobject.AbstractPage;

public class ThickboxPopup<T extends AbstractPage<T>> extends AbstractPage<T>
{
	private final T wrappedPage;

	public ThickboxPopup(T wrappedPage)
	{
		super(wrappedPage.getContext());
		this.wrappedPage = wrappedPage;
	}

	@Override
	public void checkLoaded() throws Error
	{
		driver.switchTo().frame("TB_iframeContent");
		wrappedPage.checkLoaded();
	}

	@Override
	public T get()
	{
		super.get();
		return wrappedPage.get();
	}

}
