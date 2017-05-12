package com.tle.webtests.test.accessibility;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.page.PasswordDialog;
import com.tle.webtests.pageobject.generic.page.UserProfilePage;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("fiveo")
public class AccessibilityModeTest extends AbstractCleanupTest
{
	@Test
	public void acModeTest()
	{
		logon("acmodeuser", "icantsee");
		UserProfilePage profilePage = new UserProfilePage(context).load();

		Assert.assertFalse(profilePage.isAccessibleDropdown());
		profilePage.setAccessibilityMode(true);
		profilePage.saveSuccesful();

		Assert.assertTrue(profilePage.isAccessibilityMode());
		Assert.assertTrue(profilePage.isAccessibleDropdown());

		PasswordDialog changePass = profilePage.changePasswordDialog();
		// TODO Would be better as a check on all dialogs when loaded
		Assert.assertTrue(changePass.accessibilityElementExists());
		changePass.close(profilePage);

		logout();
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("acmodeuser", "icantsee");
		UserProfilePage profilePage = new UserProfilePage(context).load();
		if( profilePage.isAccessibilityMode() )
		{
			profilePage.setAccessibilityMode(false);
			profilePage.saveSuccesful();
		}
	}
}
