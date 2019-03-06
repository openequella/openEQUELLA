package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class ShareSearchQuerySection extends AbstractPage<ShareSearchQuerySection>
{
	@FindBy(id = "sra_share")
	private WebElement openButton;

	@FindBy(id = "sssq_r")
	private WebElement rssFeedLink;

	@FindBy(id = "sssq_a")
	private WebElement atomFeedLink;

	@FindBy(id = "sssq_u")
	private WebElement urlField;

	@FindBy(id = "sssq_e")
	private WebElement emailField;

	@FindBy(id = "sssq_g")
	private WebElement guestOnlyCheck;

	@FindBy(id = "sssq_seb")
	private WebElement sendEmailButton;

	public ShareSearchQuerySection(PageContext context)
	{
		super(context, By.className("sharesearchquery"));
	}

	public ShareSearchQuerySection open()
	{
		openButton.click();
		return get();
	}

	public String getRssUrl()
	{
		return rssFeedLink.getAttribute("href");
	}

	public String getAtomUrl()
	{
		return atomFeedLink.getAttribute("href");
	}

	public String getShareUrl()
	{
		return urlField.getText();
	}

	public void setEmail(String emailAddress)
	{
		urlField.sendKeys(emailAddress);
	}

	public void setGuestOnly(boolean guestOnly)
	{
		if( guestOnlyCheck.isSelected() != guestOnly )
		{
			guestOnlyCheck.click();
		}
	}

	public void sendEmail()
	{
		sendEmailButton.click();
	}
}