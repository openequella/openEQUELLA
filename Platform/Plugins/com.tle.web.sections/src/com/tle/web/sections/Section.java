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

package com.tle.web.sections;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.SectionEvent;

/**
 * A {@code Section} is a Web Component.
 *
 * <p>It begins it's life by becoming registered into a {@link SectionTree} and given it's own ID
 * which is guaranteed to be unique within that {@code SectionTree}. During the registration
 * process, it can register itself as a listener for various {@link SectionEvent} types. It can also
 * register child {@code Section}s, which can in turn do their own registration until eventually the
 * {@code SectionTree} is finished and can be added to a {@link SectionInfo} object and used for
 * processing.
 *
 * @author jmaginnis
 */
@NonNullByDefault
public interface Section extends SectionId {
  /**
   * Called after the section has been registered into a {@link SectionTree}.
   *
   * @param id The id that this section has been registered with ( which is not necessarily the same
   *     as {@link #getDefaultPropertyName()} )
   * @param tree The tree that this section has been registered with
   */
  void registered(String id, SectionTree tree);

  boolean isTreeIndexed();

  /**
   * Called after the entire SectionTree has been registered.
   *
   * @param id The id that this section has been registered with ( which is not necessarily the same
   *     as {@link #getDefaultPropertyName()} )
   * @param tree The tree that this section has been registered with
   */
  void treeFinished(String id, SectionTree tree);

  /**
   * Create an instance of the Model. This is called by the SectionInfo object and will be cached
   * there.
   *
   * @return An instance of the Model.
   * @throws Exception
   */
  Object instantiateModel(SectionInfo info);

  /**
   * The default property name is the name that the SectionTree will try and use to register this
   * Section with.<br>
   * Generally it should be a small but human readable string. Any request parameters that this
   * section uses should be prefixed with this string.
   *
   * @return The default name for this section
   */
  String getDefaultPropertyName();
}
