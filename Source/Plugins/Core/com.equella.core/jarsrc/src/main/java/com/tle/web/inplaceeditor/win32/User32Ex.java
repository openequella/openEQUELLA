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

import com.sun.jna.platform.win32.User32;

/** @author Aaron */
public interface User32Ex extends User32 {
  // public static final User32Ex USER32 = (User32Ex)
  // Native.loadLibrary("user32", User32Ex.class,
  // W32APIOptions.UNICODE_OPTIONS);
  //
  // HWND GetParent(HWND hWnd);
  //
  // HWND GetWindow(HWND hWnd, int flag);
  //
  // HWND SetFocus(HWND hWnd);
}
