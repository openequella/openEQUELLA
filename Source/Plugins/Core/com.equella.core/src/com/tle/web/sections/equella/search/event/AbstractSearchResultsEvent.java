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

package com.tle.web.sections.equella.search.event;

import com.tle.web.sections.events.AbstractSectionEvent;
import java.util.EventListener;

public abstract class AbstractSearchResultsEvent<E extends AbstractSearchResultsEvent<E>>
    extends AbstractSectionEvent<SearchResultsListener<E>> {
  protected boolean errored;
  protected String errorMessage;

  public boolean isErrored() {
    return errored;
  }

  public void setErrored(boolean errored) {
    this.errored = errored;
  }

  /**
   * @return the errorMessage
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * @param errorMessage the errorMessage to set
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public abstract int getOffset();

  public abstract int getCount();

  public abstract int getMaximumResults();

  public abstract int getFilteredOut();

  @Override
  public Class<? extends EventListener> getListenerClass() {
    return SearchResultsListener.class;
  }
}
