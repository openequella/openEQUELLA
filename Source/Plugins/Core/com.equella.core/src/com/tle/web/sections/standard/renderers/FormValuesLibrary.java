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

package com.tle.web.sections.standard.renderers;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;

@SuppressWarnings("nls")
public final class FormValuesLibrary {
  private static final IncludeFile INCLUDE =
      new IncludeFile(
          ResourcesService.getResourceHelper(FormValuesLibrary.class).url("js/formvalues.js"));

  public static final JSCallable IS_SOME_CHECKED =
      new ExternallyDefinedFunction("isSomeChecked", 1, INCLUDE);
  public static final JSCallable GET_CHECKED_VALUES =
      new ExternallyDefinedFunction("getCheckedValues", 1, INCLUDE);
  public static final JSCallable GET_CHECK_VALUE =
      new ExternallyDefinedFunction("getCheckedValue", 1, INCLUDE);
  public static final JSCallable GET_ALL_VALUES =
      new ExternallyDefinedFunction("getAllFormValues", 1, INCLUDE);
  public static final JSCallable SET_ALL_DISABLED_STATE =
      new ExternallyDefinedFunction("setAllDisabledState", 2, INCLUDE);
  public static final JSCallable SET_ALL_CHECKED_STATE =
      new ExternallyDefinedFunction("setAllCheckedState", 2, INCLUDE);

  private FormValuesLibrary() {
    throw new Error();
  }
}
