package com.tle.webtests.remotetest.integration.canvas.init;

import com.tle.webtests.pageobject.integration.canvas.course.CanvasModulePage;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasSettingsPage;
import com.tle.webtests.pageobject.integration.canvas.course.settings.CanvasAppsTab;
import com.tle.webtests.remotetest.integration.canvas.AbstractCanvasTest;
import org.testng.annotations.Test;

@Test(groups = "init")
public class SyncCanvasTest extends AbstractCanvasTest {
  @Test
  public void deleteTools() {
    CanvasSettingsPage settings = getToCourseSettings(COURSE_NAME);
    CanvasAppsTab apps = settings.apps();
    apps.deleteAllApps(EXTERNAL_TOOL_NAME);
    apps.logout();
  }

  @Test
  public void resetModule() {
    CanvasModulePage modules = getToCourseModules(COURSE_NAME);
    modules.deleteModule(MODULE_NAME);
    modules.addModule(MODULE_NAME);
    modules.logout();
  }

  @Override
  protected boolean isCleanupItems() {
    return false;
  }
}
