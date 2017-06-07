package com.tle.webtests.pageobject.multidb;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.institution.DatabasesPage;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.function.Function;

public class InstallPage extends AbstractPage<InstallPage>
{
	@FindBy(id = "isi_emails")
	private WebElement emailsField;
	@FindBy(id = "isi_smtpServer")
	private WebElement smtpField;
	@FindBy(id = "isi_noReplySender")
	private WebElement noReplyField;
	@FindBy(id = "isi_password")
	private WebElement passwordField;
	@FindBy(id = "isi_passwordConfirm")
	private WebElement passwordConfirmField;
	@FindBy(id = "isi_licenceField")
	private WebElement licenceField;
	@FindBy(id = "isi_installButton")
	private WebElement installButton;

	public InstallPage(PageContext context)
	{
		super(context, By.id("isi_password"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "institutions.do");
	}

	public void setEmails(String emails)
	{
		emailsField.clear();
		emailsField.sendKeys(emails);
	}

	public void setSmtpServer(String smtp)
	{
		smtpField.clear();
		smtpField.sendKeys(smtp);
	}

	public void setNoReply(String email)
	{
		noReplyField.clear();
		noReplyField.sendKeys(email);
	}

	public void setPassword(String password)
	{
		passwordField.clear();
		passwordField.sendKeys(password);
	}

	public DatabasesPage install()
	{
		installButton.click();
		return new DatabasesPage(context).get();
	}

	public boolean isPasswordError()
	{
		return isPresent(By.xpath("id('isi_password')/../p[contains(@class, 'ctrlinvalid')]"));
	}

	public boolean isEmailsError()
	{
		return isPresent(By.xpath("id('isi_emails')/../p[contains(@class, 'ctrlinvalid')]"));
	}

	public boolean isNoReplyError()
	{
		return isPresent(By.xpath("id('isi_noReplySender')/../p[contains(@class, 'ctrlinvalid')]"));
	}

	public boolean isStmpError()
	{
		return isPresent(By.xpath("id('isi_smtpServer')/../p[contains(@class, 'ctrlinvalid')]"));

	}

	public InstallPage installInvalid(Function<InstallPage, Boolean> waitTill)
	{
		installButton.click();
		getWaiter().until(wd -> waitTill);
		return get();
	}

	public void setPasswordConfirm(String string)
	{
		passwordConfirmField.clear();
		passwordConfirmField.sendKeys(string);
	}
}
