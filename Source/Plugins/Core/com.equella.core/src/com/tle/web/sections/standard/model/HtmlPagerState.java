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

package com.tle.web.sections.standard.model;

import com.tle.web.sections.render.Label;

public class HtmlPagerState extends HtmlComponentState {
  private int current = 1;
  private int startPage;
  private int endPage;
  private int lastPage;

  private Label firstLabel;
  private Label lastLabel;
  private Label prevLabel;
  private Label nextLabel;

  public int getCurrent() {
    return current;
  }

  public void setCurrent(int current) {
    this.current = current;
  }

  public int getStartPage() {
    return startPage;
  }

  public int getEndPage() {
    return endPage;
  }

  public int getLastPage() {
    return lastPage;
  }

  public void setup(int availablePages, int maxDisplayed) {
    lastPage = availablePages;
    int pagerPages = lastPage > maxDisplayed ? maxDisplayed : lastPage;
    startPage = current - (pagerPages / 2) + 1;
    if (startPage > (lastPage - pagerPages + 1)) {
      startPage = lastPage - pagerPages + 1;
    }
    if (startPage < 1) {
      startPage = 1;
    }
    endPage = startPage + (pagerPages - 1);
  }

  public Label getFirstLabel() {
    return firstLabel;
  }

  public void setFirstLabel(Label firstLabel) {
    this.firstLabel = firstLabel;
  }

  public Label getLastLabel() {
    return lastLabel;
  }

  public void setLastLabel(Label lastLabel) {
    this.lastLabel = lastLabel;
  }

  public Label getPrevLabel() {
    return prevLabel;
  }

  public void setPrevLabel(Label prevLabel) {
    this.prevLabel = prevLabel;
  }

  public Label getNextLabel() {
    return nextLabel;
  }

  public void setNextLabel(Label nextLabel) {
    this.nextLabel = nextLabel;
  }
}
