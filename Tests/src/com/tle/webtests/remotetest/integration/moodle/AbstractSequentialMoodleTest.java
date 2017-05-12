package com.tle.webtests.remotetest.integration.moodle;

import com.tle.webtests.pageobject.integration.moodle.MoodleEquellaSettingsPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleModAdminPage;

public abstract class AbstractSequentialMoodleTest extends AbstractMoodleTest
{
	/**
	 * Reset the settings
	 * 
	 * @return the settings page
	 */
	protected MoodleEquellaSettingsPage resetSettings()
	{
		MoodleEquellaSettingsPage settings = new MoodleModAdminPage(context).load().equellaSettings();
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

		return settings;
	}
}
