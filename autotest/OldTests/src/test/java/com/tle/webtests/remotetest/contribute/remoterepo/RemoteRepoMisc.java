package com.tle.webtests.remotetest.contribute.remoterepo;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.Test;

/*
 * For miscellaneous RemoteRepo tests that do not apply to any specific type
 */
@TestInstitution("fiveo")
public class RemoteRepoMisc extends AbstractCleanupAutoTest {
  private final String bothCollection = "Basic Items";
  private final String remoteOnlyCollection = "RemoteRepoOnly";

  @Test
  public void testRemoteRepoOnlyCollection() {
    // Browse to contribute page
    MenuSection ms = new MenuSection(context).get();
    ContributePage contribPage = ms.clickMenu("Contribute", new ContributePage(context));

    // Normal collection (with remoterepos) is link and not text
    assertTrue(contribPage.hasCollection(bothCollection));

    // Remote repo only collection is text and not a link
    assertTrue(contribPage.hasRemoteRepoOnlyCollection(remoteOnlyCollection));
  }
}
