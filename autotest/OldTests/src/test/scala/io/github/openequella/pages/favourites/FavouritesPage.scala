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

package io.github.openequella.pages.favourites

import com.tle.webtests.framework.PageContext
import io.github.openequella.pages.search.AbstractSearchPage
import org.openqa.selenium.By

class FavouritesPage(
    context: PageContext
) extends AbstractSearchPage[FavouritesPage](context) {
  override def loadUrl(): Unit =
    driver.get(context.getBaseUrl + "page/favourites")

  /** Selects the 'Resources' type of favourites to display.
    */
  def selectFavouritesResourcesType(): Unit = selectFavouritesType("Resources")

  /** Selects the 'Searches' type of favourites to display.
    */
  def selectFavouritesSearchesType(): Unit = selectFavouritesType("Searches")

  /** Checks if a search with the given name exists in favourites.
    */
  def hasSearch(name: String): Boolean = hasItem(name)

  /** Selects a search with the given name from favourites.
    */
  def selectSearch(name: String): Unit = selectLink(name)

  /** Remove a favourite item or search with the given name from favourites page.
    */
  def removeFavourite(name: String): Unit = {
    val searchLink = findLink(name)
    val removeButton =
      searchLink.findElement(By.xpath("./../..//button[@aria-label='Remove from favourites']"))

    removeButton.click()
    confirmDialog()

    waitForSearchCompleted()
  }

  private def selectFavouritesType(favouriteType: String): Unit = {
    // Make sure the current page is finishing loading before switching the type to fix some flaky test issues.
    waitForSearchCompleted()

    val searchesTypeButton = By.xpath("//button[@aria-label='" + favouriteType + "']")

    driver.findElement(searchesTypeButton).click()

    waitForSearchCompleted()
  }
}
