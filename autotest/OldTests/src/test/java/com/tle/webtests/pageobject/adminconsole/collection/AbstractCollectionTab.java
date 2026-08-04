package com.tle.webtests.pageobject.adminconsole.collection;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.adminconsole.AbstractAppletWindow;
import org.fest.swing.fixture.WindowFixture;

public abstract class AbstractCollectionTab extends AbstractAppletWindow<AbstractCollectionTab> {

  public AbstractCollectionTab(PageContext context, WindowFixture<?> windowHolder) {
    super(context);
    this.windowHolder = windowHolder;
  }

  public CollectionEditorTab editor() {
    clickTab("Editor");
    return new CollectionEditorTab(context, windowHolder);
  }

  public CollectionDetailsTab details() {
    clickTab("Details");
    return new CollectionDetailsTab(context, windowHolder);
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
