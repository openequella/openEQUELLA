/** */
package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.CourseDefaultsPage;
import com.tle.webtests.test.AbstractSessionTest;
import java.util.TimeZone;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class CourseDefaultsTest extends AbstractSessionTest {
  @Test
  public void testChangeDates() {
    logon("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    CourseDefaultsPage cdp = sp.courseDefaultsSettings();

    // Midnight UTC dates, which are 'conceptual'
    java.util.Calendar[] nowAndTheFuture = getNowRange();
    cdp.setStartDate(nowAndTheFuture[0]);
    cdp.setEndDate(nowAndTheFuture[1]);
    cdp.save();
    logout();

    logon("AutoTest", "automated");
    sp = new SettingsPage(context).load();
    cdp = sp.courseDefaultsSettings();

    com.tle.webtests.pageobject.generic.component.Calendar sd = cdp.getStartDate(context);
    assertTrue(sd.dateEquals(nowAndTheFuture[0]), "Reflected start date is incorrect");

    com.tle.webtests.pageobject.generic.component.Calendar ed = cdp.getEndDate(context);
    assertTrue(ed.dateEquals(nowAndTheFuture[1]), "Reflected end date is incorrect");
  }

  protected java.util.Calendar[] getNowRange() {
    return com.tle.webtests.pageobject.generic.component.Calendar.getDateRange(
        TimeZone.getTimeZone("Etc/UTC"), false, false);
  }
}
