package com.tle.webtests.remotetest.integration.moodle;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.UndeterminedPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleAdminPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleCronPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleEquellaSettingsPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleManageRepoPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleModAdminPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleNotificationsPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleSettingsUpgradePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleUpgradeDatabasePage;

@TestInstitution("moodle")
public class SyncMoodleTest extends AbstractMoodleTest
{
	@Test(dependsOnMethods = "doUpgradeIfRequired")
	public void sync()
	{
		MoodleLoginPage loginPage = new MoodleLoginPage(context).load();
		MoodleEquellaSettingsPage settings;
		loginPage.logon("admin", "admin");
		MoodleModAdminPage moodleModAdminPage = new MoodleModAdminPage(context).load();
		if( moodleModAdminPage.moduleExists("EQUELLA Resource") )
		{
			moodleModAdminPage.deleteModule("EQUELLA Resource");
		}

		settings = new MoodleAdminPage(context).load().upgrade(new MoodleEquellaSettingsPage(context));
		settings.setUrl(context.getBaseUrl() + "signon.do");
		settings.setAction("selectOrAdd");
		settings.setNewWindow(false);
		settings.setOptions("");
		settings.setSecret("token", "token");
		settings.setRestriction("none");
		settings.setTeacherSecret("teacher", "teacher");
		settings.setManagerSecret("manager", "manager");
		settings.setAdminUser("AutoTest");
		settings.enableLti(false);

		settings.save();

		MoodleManageRepoPage manageRepoPage = new MoodleManageRepoPage(context).load();
		manageRepoPage.enableEquella();

		new MoodleCronPage(context).load();
	}

	@Test
	public void doUpgradeIfRequired()
	{
		MoodleLoginPage loginPage = new MoodleLoginPage(context).load();
		MoodleIndexPage mip = new MoodleIndexPage(context);
		MoodleUpgradeDatabasePage dbPage = new MoodleUpgradeDatabasePage(context);
		UndeterminedPage<PageObject> up = loginPage.logonToUndetermined("admin", "admin", mip, dbPage);
		PageObject unknown = up.get();

		if( unknown == dbPage )
		{
			MoodleLoginPage lp = dbPage.clickContinue().clickContinue().clickUpgradeNow().clickContinue();
			MoodleNotificationsPage mnp = new MoodleNotificationsPage(context);
			MoodleSettingsUpgradePage msup = new MoodleSettingsUpgradePage(context);
			UndeterminedPage<PageObject> mup = new UndeterminedPage<PageObject>(context, mnp, msup);
			unknown = mup.get();
			if( unknown == msup )
			{
				msup.saveChanges();
			}

			mnp.logout();
		}
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		// no cleanup
	}
}