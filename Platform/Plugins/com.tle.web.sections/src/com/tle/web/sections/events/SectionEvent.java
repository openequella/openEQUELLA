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

package com.tle.web.sections.events;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import java.util.EventListener;

/**
 * An event that is to be processed by <code>EventListener</code>s.
 *
 * <p>There are three different types of <code>SectionEvent</code>s.
 *
 * <ul>
 *   <li>Broadcast - These are events which are broadcast to listeners registered with a target id
 *       of <code>null</code>. Examples are:
 *       <ul>
 *         <li>{@link ParametersEvent}
 *         <li>{@link BookmarkEvent}
 *       </ul>
 *       <p>
 *   <li>Targeted<br>
 *       These are events which are directed towards are particular {@link
 *       com.tle.web.sections.Section}. <br>
 *       A good example is the {@link RenderEvent}.
 *       <p>
 *   <li>Direct - These events don't have a listener list, they are just fired directly via {@link
 *       #fireDirect(SectionId, SectionInfo)}. <br>
 *       An example is the {@link
 *       com.tle.web.sections.registry.handler.AnnotatedEventsScanner.DirectMethodEvent}.
 *       <p>
 * </ul>
 *
 * The {@link SectionInfo} object is responsible for maintaining a priority queue of <code>
 * SectionEvent</code>s. The queue can be bypassed by directly calling {@link
 * SectionInfo#processEvent(SectionEvent)}, which is generally the case for Broadcast and Targeted
 * events.
 *
 * @author jmaginnis
 * @see SectionInfo#processEvent(SectionEvent)
 * @see SectionInfo#queueEvent(SectionEvent)
 */
@NonNullByDefault
public interface SectionEvent<L extends EventListener> extends Comparable<SectionEvent<L>> {
  int PRIORITY_HIGH = 100;
  int PRIORITY_BEFORE_EVENTS = 60;
  int PRIORITY_EVENTS = 50;
  int PRIORITY_MODAL_LOGIC = 40;
  int PRIORITY_AFTER_EVENTS = 25;
  int PRIORITY_NORMAL = 0;
  int PRIORITY_LOW = -100;
  int PRIORITY_AFTER_EVENTS_BEFORE_NORMAL = 24;

  /**
   * Get the priority of this event. <br>
   * Higher the number, higher the priority.
   *
   * @return The priority of this event
   */
  int getPriority();

  /**
   * Get the id of the listener list.
   *
   * @return The id of the listener list, <code>null</code> if it is a broadcast event.
   */
  @Nullable
  String getListenerId();

  /**
   * Gets the id of the <code>Section</code> this event is for.
   *
   * @return The id of the <code>Section</code> or <code>null</code> if this event is not for a
   *     particular <code>Section</code>
   */
  SectionId getForSectionId();

  /**
   * Get the listener class this event is for.
   *
   * @return The <code>EventListener</code> subclass that this event uses (if it is a Broadcast of
   *     Targeted event).
   */
  @Nullable
  Class<? extends EventListener> getListenerClass();

  /**
   * Called by the {@code SectionInfo} object when it wants to fire a Broadcast or Targeted event to
   * a listener. <br>
   * Generally this method will just cast the listener to it's appropriate subclass and call a
   * method on it.
   *
   * @param sectionId The {@link com.tle.web.sections.SectionContext} of <code>Section</code>, if it
   *     is associated with one.
   * @param info The {@code SectionInfo} that is firing this event.
   * @param listener The EventListener to call
   * @throws Exception TODO
   * @throws Exception
   * @see #getForSectionId()
   */
  void fire(SectionId sectionId, SectionInfo info, @Nullable L listener) throws Exception;

  /**
   * Called before firing the event to any listeners.
   *
   * @param info The {@code SectionInfo} that processed this event.
   */
  void beforeFiring(SectionInfo info, @Nullable SectionTree tree);

  /**
   * Called after firing this event to all listeners.
   *
   * @param info The {@code SectionInfo} that processed this event.
   */
  void finishedFiring(SectionInfo info, @Nullable SectionTree tree);

  /**
   * You will generally use this in your fire method, eg: return event.isStopProcessing(); Your
   * event handlers may have the option of setting setStopProcessing(true|false) if they really want
   * to.
   *
   * @return will the event keep processing?
   */
  boolean isStopProcessing();

  void stopProcessing();

  boolean isAbortProcessing();

  void abortProcessing();

  boolean isContinueAfterException();
}
