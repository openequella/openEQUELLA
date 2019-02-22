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

package com.tle.web.sections.events;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import java.util.EventListener;

public abstract class AbstractSectionEvent<L extends EventListener> implements SectionEvent<L> {
  private boolean stopProcessing;
  private boolean abortProcessing;

  // Broadcast

  @Override
  public SectionId getForSectionId() {
    return null;
  }

  private int priority;

  @Override
  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  @Override
  public String getListenerId() {
    return null;
  }

  @Override
  public void beforeFiring(SectionInfo info, SectionTree tree) {
    this.stopProcessing = false;
  }

  @Override
  public void finishedFiring(SectionInfo info, SectionTree tree) {
    // nothing
  }

  @Override
  public int compareTo(SectionEvent<L> o) {
    return o.getPriority() - getPriority();
  }

  @Override
  public boolean isStopProcessing() {
    return stopProcessing;
  }

  @Override
  public void stopProcessing() {
    this.stopProcessing = true;
  }

  @Override
  public boolean isAbortProcessing() {
    return abortProcessing;
  }

  @Override
  public void abortProcessing() {
    this.abortProcessing = true;
  }

  @Override
  public boolean isContinueAfterException() {
    return false;
  }
}
