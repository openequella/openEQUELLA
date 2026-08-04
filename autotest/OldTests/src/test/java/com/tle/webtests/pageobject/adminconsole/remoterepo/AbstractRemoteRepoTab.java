package com.tle.webtests.pageobject.adminconsole.remoterepo;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.adminconsole.AbstractAppletWindow;
import org.fest.swing.fixture.WindowFixture;

public abstract class AbstractRemoteRepoTab extends AbstractAppletWindow<AbstractRemoteRepoTab> {

  public AbstractRemoteRepoTab(PageContext context, WindowFixture<?> windowHolder) {
    super(context);
    this.windowHolder = windowHolder;
  }

  public RemoteRepoDetailsTab details() {
    clickTab("Details");
    return new RemoteRepoDetailsTab(context, windowHolder);
  }

  public void save() {
    clickButton("Save");
    clickButton("OK");
    clickButton("Close");
  }

  public void close() {
    clickButton("Close");
  }
}
