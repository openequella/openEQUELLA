package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.generic.component.MultiLingualEditbox;

public class BrowsePortalEditPage extends AbstractPortalEditPage<BrowsePortalEditPage> {
  public BrowsePortalEditPage(PageContext context) {
    super(context);
  }

  @Override
  public String getType() {
    return "Browse";
  }

  @Override
  public String getId() {
    return "ebrs";
  }

  /**
   * Set the multi-language title for the portal.
   *
   * @param englishTitle The title in English.
   * @param afarTitle The title in Afar.
   */
  public BrowsePortalEditPage setMultiLanguageTitle(String englishTitle, String afarTitle) {
    MultiLingualEditbox multiLang = getTitleSection();
    multiLang.allMode();
    multiLang.editLangString("English", englishTitle);
    multiLang.editLangString("Afar", afarTitle);
    return this;
  }
}
