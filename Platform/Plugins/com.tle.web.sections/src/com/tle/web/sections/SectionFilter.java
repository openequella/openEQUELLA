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

package com.tle.web.sections;

import com.tle.web.sections.header.HeaderHelper;

/**
 * Filter a Sections request/forward.
 *
 * <p>Upon {@link SectionInfo} creation, a list of prioritized {@code SectionFilter}s have a chance
 * run and manipulate the {@code SectionInfo}.
 *
 * <p>Usually this manipulation involves adding a {@link SectionTree} to wrap the existing tree(s).
 *
 * <p>The requested page's {@code SectionTree} will always be wrapped by at least one {@code
 * SectionTree} which is responsible for implementing the {@link HeaderHelper} and rendering any
 * header/footer and navigation.
 *
 * @author jmaginnis
 */
public interface SectionFilter {
  /**
   * Filter this request/forward.
   *
   * @param info The {@code SectionInfo} to filter
   */
  void filter(MutableSectionInfo info);
}
