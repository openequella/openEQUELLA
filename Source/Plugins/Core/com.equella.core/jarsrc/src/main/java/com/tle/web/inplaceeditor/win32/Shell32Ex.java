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

package com.tle.web.inplaceeditor.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.win32.W32APIOptions;

/** @author Aaron */
public interface Shell32Ex extends Shell32 {
  @SuppressWarnings("nls")
  Shell32Ex SHELL32 =
      (Shell32Ex) Native.loadLibrary("shell32", Shell32Ex.class, W32APIOptions.UNICODE_OPTIONS);

  HINSTANCE ShellExecute(
      int hWnd, String lpVerb, String lpFile, String lpParameters, String lpDirectory, int nShow);
}
