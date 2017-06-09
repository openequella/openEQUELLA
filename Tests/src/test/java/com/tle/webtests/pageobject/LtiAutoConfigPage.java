package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;

public class LtiAutoConfigPage extends AbstractPage<LtiAutoConfigPage>
{

	public LtiAutoConfigPage(PageContext context)
	{
		super(context);
	}

	protected void checkLoadedElement()
	{
		// I'm sure its fine..
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "lti/autoconfig");
	}

	public String getXML()
	{
		String xml = driver.getPageSource();
		int start = xml.indexOf("<cartridge_basiclti_link");
		int end = xml.indexOf("</cartridge_basiclti_link>") + "</cartridge_basiclti_link>".length();
		return xml.substring(start, end);
	}

}
