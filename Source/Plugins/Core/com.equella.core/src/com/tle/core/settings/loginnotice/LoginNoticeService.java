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

package com.tle.core.settings.loginnotice;

import com.tle.core.settings.loginnotice.impl.PreLoginNotice;
import java.io.IOException;
import java.io.InputStream;

public interface LoginNoticeService {

  PreLoginNotice getPreLoginNotice() throws IOException;

  void setPreLoginNotice(PreLoginNotice notice) throws IOException;

  void deletePreLoginNotice();

  String uploadPreLoginNoticeImage(InputStream imageFile, String name) throws IOException;

  String getMimeType(String name) throws IOException;

  InputStream getPreLoginNoticeImage(String name) throws IOException;

  String getPostLoginNotice();

  void setPostLoginNotice(String notice);

  void deletePostLoginNotice();

  void checkPermissions();

  boolean isActive(PreLoginNotice preLoginNotice);
}
