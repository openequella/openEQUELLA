package com.tle.webtests.pageobject.generic.page;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class VerifyableAttachment extends AbstractPage<VerifyableAttachment>
{
	public VerifyableAttachment(PageContext context)
	{
		super(context, By.xpath("//p[text()='This is a verifiable attachment']"));
	}

	public VerifyableAttachment(PageContext context, String bodyText)
	{
		super(context, By.xpath("//body//*[contains(text(), " + quoteXPath(bodyText) + ")]"));
	}

	public boolean isVerified()
	{
		return isPresent(loadedBy);
	}
}
