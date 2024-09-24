/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class OidcSettingsPage extends AbstractPage<OidcSettingsPage> {
  public static final String TITLE = "OIDC configuration";

  public static String getUrl() {
    return "page/oidc";
  }

  public OidcSettingsPage(PageContext context) {
    super(context, By.xpath("//h5[text()='" + TITLE + "']"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + getUrl());
  }
}
