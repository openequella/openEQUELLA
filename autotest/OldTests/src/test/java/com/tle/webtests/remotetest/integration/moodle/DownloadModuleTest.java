package com.tle.webtests.remotetest.integration.moodle;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.integration.moodle.MoodleModuleDownloadPage;
import com.tle.webtests.test.AbstractTest;

@TestInstitution("moodle")
public class DownloadModuleTest extends AbstractTest {
  // @Test
  public void downloadModules() {
    new MoodleModuleDownloadPage(context, testConfig.getIntegrationUrl("moodlebase")).load();
  }
}
