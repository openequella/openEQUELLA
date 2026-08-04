package com.tle.webtests.pageobject.adminconsole.collection;

import com.tle.webtests.framework.PageContext;
import org.fest.swing.fixture.WindowFixture;

public class CollectionDetailsTab extends AbstractCollectionTab {

  public CollectionDetailsTab(PageContext context, WindowFixture<?> windowHolder) {
    super(context, windowHolder);
  }

  public CollectionDetailsTab setName(String name) {
    setText(0, name);
    return this;
  }

  public CollectionDetailsTab setDescription(String description) {
    setText(1, description);
    return this;
  }

  public CollectionDetailsTab setMetadata(String metadata) {
    clickComboBox(metadata);
    return this;
  }
}
