package com.tle.webtests.test.users;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.page.UserProfilePage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class DisplayDateFormatTest extends AbstractSessionTest {
  @Test
  public void testSavePersonalDateFormatOptions() {
    logon("AutoTest", "automated");
    UserProfilePage userPage = new UserProfilePage(context).load();
    userPage.setDateFormat("format.approx");
    userPage.saveSuccesful();
    logout();

    logon("AutoTest", "automated");
    userPage = new UserProfilePage(context).load();
    assertEquals(userPage.getDateFormat(), "Relative date - e.g about an hour ago");
    logout();
  }
}
