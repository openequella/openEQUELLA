package com.tle.webtests.pageobject;

import com.google.common.collect.Lists;
import com.tle.webtests.framework.PageContext;
import java.util.List;
import org.openqa.selenium.NotFoundException;

public class UndeterminedPage<T extends PageObject> extends AbstractPage<T> {
  private List<AbstractPage<? extends T>> choices = Lists.newArrayList();
  private AbstractPage<? extends T> determinedChoice;

  public UndeterminedPage(
      PageContext context, AbstractPage<? extends T> page1, AbstractPage<? extends T> page2) {
    super(context);
    add(page1);
    add(page2);
  }

  public UndeterminedPage<T> add(AbstractPage<? extends T> page) {
    choices.add(page);
    return this;
  }

  @Override
  public void loadUrl() {
    choices.get(0).loadUrl();
  }

  @Override
  protected T actualPage() {
    return determinedChoice.get();
  }

  @Override
  public void checkLoaded() {
    NotFoundException firstError = null;
    for (AbstractPage<? extends T> choice : choices) {
      try {
        choice.checkLoaded();
        determinedChoice = choice;
        return;
      } catch (NotFoundException nfe) {
        if (firstError == null) {
          firstError = nfe;
        }
      }
    }
    throw firstError;
  }
}
