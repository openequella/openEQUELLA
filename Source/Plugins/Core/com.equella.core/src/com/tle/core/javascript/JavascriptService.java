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

package com.tle.core.javascript;

import com.tle.common.NameValue;
import com.tle.common.beans.exception.NotFoundException;
import java.util.List;

public interface JavascriptService {
  /**
   * Get all JavaScript libraries as display-name/id pairs.
   *
   * @return all JavaScript libraries in name-value form
   */
  List<NameValue> listLibraries();

  /**
   * Get all JavaScript modules for a given JavaScript library as display-name/id pairs.
   *
   * @param libraryId the JavaScript library ID
   * @return all modules for the library in name-value form, sorted case-insensitively by name
   * @throws NotFoundException if no library exists with the given ID
   */
  List<NameValue> modulesByLibraryId(String libraryId);

  List<JavascriptLibrary> getAllJavascriptLibraries();

  JavascriptLibrary getJavascriptLibrary(String libraryId);

  JavascriptModule getJavascriptModule(String libraryId, String moduleId);
}
