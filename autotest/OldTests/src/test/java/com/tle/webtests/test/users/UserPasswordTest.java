package com.tle.webtests.test.users;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.page.PasswordDialog;
import com.tle.webtests.pageobject.generic.page.UserProfilePage;
import com.tle.webtests.pageobject.portal.TopbarMenuSection;
import com.tle.webtests.test.AbstractSessionTest;

/**
 * Recorded at 3/3/09 2:25 PM
 */
@TestInstitution("vanilla")
public class UserPasswordTest extends AbstractSessionTest
{
	@SuppressWarnings("nls")
	@Test
	public void testPasswordChanges() throws InterruptedException
	{
		logon("PasswordTest", "``````");
		TopbarMenuSection topbar = new TopbarMenuSection(context).get();
		UserProfilePage detailsTab = topbar.editMyDetails();
		PasswordDialog passwordTab = detailsTab.changePasswordDialog();
		passwordTab.changePassword("``````", "automated1", "automated2");
		passwordTab.save(detailsTab.containsPasswordMatchError());

		passwordTab.changePassword("``````", "automated1", "automated1");
		passwordTab.save(detailsTab.passwordChange());

		logon("PasswordTest", "automated1");
		detailsTab = topbar.get().editMyDetails();
		passwordTab = detailsTab.changePasswordDialog();
		passwordTab.changePassword("automated1", "``````", "``````");
		passwordTab.save(detailsTab.passwordChange());

		detailsTab = topbar.get().editMyDetails();
		passwordTab = detailsTab.changePasswordDialog();
		passwordTab.changePassword("WRONG", "``````", "``````");
		passwordTab.save(detailsTab.containsWrongOldPasswordError());
	}

}
