package com.tle.webtests.remotetest.integration.canvas;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.integration.canvas.CanvasLoginPage;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasCoursePage;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasModulePage;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasSettingsPage;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasWikiPage;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("canvas")
public class AbstractCanvasTest extends AbstractCleanupTest {
  protected static final String ADMIN_USERNAME = "developmentteam@equella.com";
  protected static final String ADMIN_PASSWORD = "canvas";
  protected static final String COURSE_ID = "EQ101";
  protected static final String COURSE_NAME = "Autotest course";
  protected static final String MODULE_NAME = "autotest-module";
  protected static final String EXTERNAL_TOOL_NAME = "EQUELLA-autotest";

  @Override
  protected void customisePageContext() {
    context.setIntegUrl(testConfig.getIntegrationUrl("canvas"));
  }

  private CanvasCoursePage getToCoursePage(String course) {
    new CanvasLoginPage(context).load().logon(ADMIN_USERNAME, ADMIN_PASSWORD);
    return new CanvasCoursePage(context, COURSE_ID).load();
  }

  protected CanvasModulePage getToCourseModules(String course) {
    CanvasCoursePage canvasCourse = getToCoursePage(course);
    return canvasCourse.modules();
  }

  protected CanvasWikiPage getToCourseWiki(String course) {
    CanvasCoursePage canvasCourse = getToCoursePage(course);
    return canvasCourse.wiki();
  }

  protected CanvasSettingsPage getToCourseSettings(String course) {
    CanvasCoursePage canvasCourse = getToCoursePage(course);
    return canvasCourse.settings();
  }
}
