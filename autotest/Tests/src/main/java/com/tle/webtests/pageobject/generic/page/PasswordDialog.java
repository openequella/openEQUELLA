package com.tle.webtests.pageobject.generic.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class PasswordDialog extends AbstractPage<PasswordDialog>
{
	@FindBy(id = "ed_passDialog_oldPassword")
	private WebElement oldPasswordField;
	@FindBy(id = "ed_passDialog_newPassword")
	private WebElement newPasswordField;
	@FindBy(id = "ed_passDialog_confirmPassword")
	private WebElement confirmPasswordField;
	@FindBy(id = "ed_passDialog_ok")
	private WebElement okButton;
	@FindBy(id = "ed_passDialog_close")
	private WebElement closeButton;

	public PasswordDialog(PageContext context)
	{
		super(context, By.id("main"));
	}

	public void changePassword(String oldPassword, String newPassword, String newPasswordConfirm)
	{
		oldPasswordField.clear();
		oldPasswordField.sendKeys(oldPassword);
		newPasswordField.clear();
		newPasswordField.sendKeys(newPassword);
		confirmPasswordField.clear();
		confirmPasswordField.sendKeys(newPasswordConfirm);
	}

	public UserProfilePage save(WaitingPageObject<UserProfilePage> returnTo)
	{
		okButton.click();
		return returnTo.get();
	}

	public UserProfilePage close(WaitingPageObject<UserProfilePage> returnTo)
	{
		closeButton.click();
		return returnTo.get();
	}

	public boolean accessibilityElementExists()
	{
		return isPresent(By.className("focus"));
	}
}
