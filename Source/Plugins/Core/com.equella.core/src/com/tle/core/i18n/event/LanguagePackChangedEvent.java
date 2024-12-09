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

package com.tle.core.i18n.event;

import com.tle.core.events.ApplicationEvent;
import com.tle.core.i18n.event.listener.LanguagePackChangedListener;
import java.util.Locale;

public class LanguagePackChangedEvent extends ApplicationEvent<LanguagePackChangedListener> {
  private static final long serialVersionUID = 1L;

  private final Locale locale;

  public LanguagePackChangedEvent(Locale locale) {
    super(PostTo.POST_TO_OTHER_CLUSTER_NODES);
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }

  @Override
  public Class<LanguagePackChangedListener> getListener() {
    return LanguagePackChangedListener.class;
  }

  @Override
  public void postEvent(LanguagePackChangedListener listener) {
    listener.languageChangedEvent(this);
  }
}
