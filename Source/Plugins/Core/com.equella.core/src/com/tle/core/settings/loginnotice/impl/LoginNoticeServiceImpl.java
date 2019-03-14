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

package com.tle.core.settings.loginnotice.impl;

import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.PrivilegeRequiredException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;

@Singleton
@Bind(LoginNoticeService.class)
public class LoginNoticeServiceImpl implements LoginNoticeService {
  @Inject TLEAclManager tleAclManager;
  @Inject ConfigurationService configurationService;
  @Inject FileSystemService fileSystemService;

  private static final String PERMISSION_KEY = "EDIT_SYSTEM_SETTINGS";
  private static final String PRE_LOGIN_NOTICE_KEY = "pre.login.notice";
  private static final String POST_LOGIN_NOTICE_KEY = "post.login.notice";
  private static final String LOGIN_NOTICE_IMAGE_FOLDER_NAME = "loginnoticeimages/";

  @Override
  public String getPreLoginNotice() {
    return configurationService.getProperty(PRE_LOGIN_NOTICE_KEY);
  }

  @Override
  public void setPreLoginNotice(String notice) throws IOException {
    checkPermissions();
    if (StringUtils.isBlank(notice)) {
      configurationService.deleteProperty(PRE_LOGIN_NOTICE_KEY);
    } else {
      configurationService.setProperty(PRE_LOGIN_NOTICE_KEY, notice);
    }
  }

  @Override
  public void deletePreLoginNotice() {
    checkPermissions();
    CustomisationFile customisationFile = new CustomisationFile();
    fileSystemService.removeFile(customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME);
    configurationService.deleteProperty(PRE_LOGIN_NOTICE_KEY);
  }

  @Override
  public String uploadPreLoginNoticeImage(File imageFile) throws IOException {
    CustomisationFile customisationFile = new CustomisationFile();
    String testName = (imageFile.getName() + ".png");

    // read in image file
    BufferedImage bImage = null;
    bImage = ImageIO.read(imageFile);
    if (bImage == null) {
      throw new IllegalArgumentException("Invalid image file");
    }
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(bImage, "png", os);
    InputStream fis = new ByteArrayInputStream(os.toByteArray());
    fileSystemService.write(
        customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME + testName, fis, false);
    os.close();
    fis.close();
    return testName;
  }

  @Override
  public InputStream getPreLoginNoticeImage(String name) throws IOException {
    CustomisationFile customisationFile = new CustomisationFile();
    if (fileSystemService.fileExists(customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME + name)) {
      return fileSystemService.read(customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME + name);
    }
    return null;
  }

  @Override
  public String getPostLoginNotice() {
    return configurationService.getProperty(POST_LOGIN_NOTICE_KEY);
  }

  @Override
  public void setPostLoginNotice(String notice) {
    checkPermissions();
    if (StringUtils.isBlank(notice)) {
      configurationService.deleteProperty(POST_LOGIN_NOTICE_KEY);
    } else {
      configurationService.setProperty(POST_LOGIN_NOTICE_KEY, notice);
    }
  }

  @Override
  public void deletePostLoginNotice() {
    checkPermissions();
    configurationService.deleteProperty(POST_LOGIN_NOTICE_KEY);
  }

  private void checkPermissions() {
    if (tleAclManager
        .filterNonGrantedPrivileges(Collections.singleton(PERMISSION_KEY), false)
        .isEmpty()) {
      throw new PrivilegeRequiredException(PERMISSION_KEY);
    }
  }
}
