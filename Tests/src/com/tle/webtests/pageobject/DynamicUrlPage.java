package com.tle.webtests.pageobject;


public class DynamicUrlPage<T extends PageObject> extends AbstractPage<T>
{
	private AbstractPage<? extends T> targetPage;
	private String url;

	private DynamicUrlPage(String url, AbstractPage<? extends T> targetPage)
	{
		super(targetPage.getContext());
		this.targetPage = targetPage;
		this.url = url;
	}
	
	@Override
	protected void loadUrl()
	{
		driver.get(url);
	}
	
	@Override
	protected T actualPage()
	{
		return targetPage.get();
	}
	
	@Override
	public void checkLoaded() throws Error
	{
		targetPage.checkLoaded();
	}
	
	public static <T extends PageObject> T load(String url, AbstractPage<? extends T> targetPage)
	{
		return new DynamicUrlPage<T>(url, targetPage).load();
	}
	
	
}
