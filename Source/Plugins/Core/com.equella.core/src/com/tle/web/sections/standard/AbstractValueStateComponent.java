/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections.standard;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.model.HtmlValueState;

public abstract class AbstractValueStateComponent<
        S extends HtmlValueState, VC extends JSValueComponent>
    extends AbstractValueComponent<S, VC>
    implements ParametersEventListener, BookmarkEventListener {

  public AbstractValueStateComponent(String defaultRenderer) {
    super(defaultRenderer);
  }

  public String getStringValue(SectionInfo info) {
    HtmlValueState state = getState(info);
    return state.getValue();
  }

  public int getIntValue(SectionInfo info) {
    HtmlValueState state = getState(info);
    return Integer.parseInt(state.getValue());
  }

  @Override
  public void bookmark(SectionInfo info, BookmarkEvent event) {
    if (addToThisBookmark(info, event)) {
      String value = getBookmarkStringValue(getState(info));
      if (value != null) {
        event.setParam(getParameterId(), value);
      }
    }
  }

  protected abstract String getBookmarkStringValue(S state);

  @Override
  public void handleParameters(SectionInfo info, ParametersEvent event) {
    String value = event.getParameter(getParameterId(), false);
    if (value != null) {
      HtmlValueState state = getState(info);
      state.setValue(value);
    }
  }
}
