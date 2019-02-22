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

package com.tle.web.sections.ajax.handler;

import com.tle.common.Check;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.events.js.ParameterizedEvent;
import java.util.Objects;
import java.util.Set;

public class UpdateDomKey {
  private final SectionId modalId;
  private final Set<String> ajaxIds;
  private final ParameterizedEvent innerEvent;

  public UpdateDomKey(SectionId modalId, Set<String> ajaxIds, ParameterizedEvent innerEvent) {
    this.modalId = modalId;
    this.ajaxIds = ajaxIds;
    this.innerEvent = innerEvent;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof UpdateDomKey)) {
      return false;
    }

    UpdateDomKey other = (UpdateDomKey) obj;
    return Objects.equals(idForSectionId(modalId), idForSectionId(other.modalId))
        && Objects.equals(ajaxIds, other.ajaxIds)
        && Objects.equals(innerEvent, other.innerEvent);
  }

  private static String idForSectionId(SectionId sectionId) {
    return sectionId != null ? sectionId.getSectionId() : null;
  }

  @Override
  public int hashCode() {
    return Check.getHashCode(idForSectionId(modalId), ajaxIds, innerEvent);
  }

  public SectionId getModalId() {
    return modalId;
  }

  public Set<String> getAjaxIds() {
    return ajaxIds;
  }

  public ParameterizedEvent getInnerEvent() {
    return innerEvent;
  }
}
