package com.tle.webtests.pageobject.adminconsole.remoterepo;

import com.tle.webtests.framework.PageContext;
import org.fest.swing.fixture.WindowFixture;

public class RemoteRepoDetailsTab extends AbstractRemoteRepoTab {

  public RemoteRepoDetailsTab(PageContext context, WindowFixture<?> windowHolder) {
    super(context, windowHolder);
  }

  public RemoteRepoDetailsTab setName(String name) {
    setText(0, name);
    return this;
  }

  public RemoteRepoDetailsTab setDescription(String description) {
    setText(1, description);
    return this;
  }

  public RemoteRepoDetailsTab setUrl(String url) {
    setText(5, url);
    return this;
  }
}
