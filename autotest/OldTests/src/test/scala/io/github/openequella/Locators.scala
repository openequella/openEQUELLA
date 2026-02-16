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

package io.github.openequella

import com.tle.webtests.pageobject.AbstractPage.quoteXPath
import org.openqa.selenium.By

/** Utility object for locating elements.
  */
object Locators {

  /** Locates an element whose *direct* text node contains the given text (non-exact match). */
  def byTextContains(text: String): By =
    By.xpath(s"//*[contains(text(), ${quoteXPath(text)})]")

  /** Locates element by its data-testid attribute. */
  def byTestId(testId: String): By = By.cssSelector(s"[data-testid='$testId']")

  /** Locates element by its aria-label attribute. */
  def byAriaLabel(label: String): By = By.cssSelector(s"[aria-label='$label']")
}
