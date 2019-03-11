package com.tle.webtests.pageobject.adminconsole.collection;

import com.tle.webtests.framework.PageContext;
import org.fest.swing.fixture.WindowFixture;

public class CollectionEditorTab extends AbstractCollectionTab {

  public CollectionEditorTab(PageContext context, WindowFixture<?> windowHolder) {
    super(context, windowHolder);
  }

  public CollectionEditorTab addChild(String parent, String name) {
    return addChild(parent, name, false, false);
  }

  public CollectionEditorTab addChild(
      String parent, String name, boolean freetext, boolean powerSearch) {
    clickTreePath(parent);
    clickButton("Child");
    setText(0, name);
    if (freetext) {
      clickCheckBox("Searchable by Free Text");
    }
    if (powerSearch) {
      clickCheckBox("Index for Advanced Searches");
    }
    clickTreePath("xml");
    return this;
  }

  public CollectionEditorTab addSibling(String parent, String name) {
    clickTreePath(parent);
    clickButton("Sibling");
    setText(0, name);
    clickTreePath("xml");
    return this;
  }
}
