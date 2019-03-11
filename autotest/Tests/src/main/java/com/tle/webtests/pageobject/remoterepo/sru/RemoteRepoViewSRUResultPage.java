package com.tle.webtests.pageobject.remoterepo.sru;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoViewResultPage;
import org.openqa.selenium.By;

public class RemoteRepoViewSRUResultPage
    extends RemoteRepoViewResultPage<RemoteRepoViewSRUResultPage> {
  public RemoteRepoViewSRUResultPage(PageContext context) {
    super(context, By.xpath("//button[contains(@id, '_importButton')]"));
  }

  public RemoteRepoSRUSearchPage clickRemoteRepoBreadcrumb(String repo) {
    driver.findElement(By.xpath("id('breadcrumbs')//a[text()='" + repo + "']")).click();
    return new RemoteRepoSRUSearchPage(context);
  }
}
