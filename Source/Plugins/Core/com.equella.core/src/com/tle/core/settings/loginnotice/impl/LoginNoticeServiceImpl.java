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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.PrivilegeRequiredException;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

@Singleton
@Bind(LoginNoticeService.class)
public class LoginNoticeServiceImpl implements LoginNoticeService {

  @Inject TLEAclManager tleAclManager;
  @Inject ConfigurationService configurationService;
  @Inject FileSystemService fileSystemService;

  @Inject
  protected void setObjectMapperService(ObjectMapperService objectMapperService) {
    objectMapper = objectMapperService.createObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  private ObjectMapper objectMapper;

  private static final String PERMISSION_KEY = "EDIT_SYSTEM_SETTINGS";
  private static final String PRE_LOGIN_NOTICE_KEY = "pre.login.notice";
  private static final String POST_LOGIN_NOTICE_KEY = "post.login.notice";
  private static final String LOGIN_NOTICE_IMAGE_FOLDER_NAME = "loginnoticeimages/";

  @Override
  public PreLoginNotice getPreLoginNotice() throws IOException {
    String preLoginNotice = configurationService.getProperty(PRE_LOGIN_NOTICE_KEY);
    if (Check.isEmpty(preLoginNotice)) {
      return null;
    }
    return objectMapper.readValue(preLoginNotice, PreLoginNotice.class);
  }

  @Override
  public void setPreLoginNotice(PreLoginNotice notice) throws IOException {
    checkPermissions();
    if (StringUtils.isBlank(notice.getNotice())) {
      configurationService.deleteProperty(PRE_LOGIN_NOTICE_KEY);
    } else {
      if (notice.getStartDate().isAfter(notice.getEndDate())) {
        throw new BadRequestException("Invalid date range.");
      }
      configurationService.setProperty(
          PRE_LOGIN_NOTICE_KEY, objectMapper.writeValueAsString(notice));
    }
    cleanUpUnusedImages(notice.getNotice());
  }

  private boolean validateDates(ZonedDateTime start, ZonedDateTime end) {
    ZonedDateTime now = ZonedDateTime.now(start.getZone());
    return (!now.isBefore(start)) && (now.isBefore(end));
  }

  private void cleanUpUnusedImages(String notice) throws IOException {
    CustomisationFile customisationFile = new CustomisationFile();
    FileEntry[] fileNameList =
        fileSystemService.enumerate(customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME, null);
    Elements imgList = Jsoup.parse(notice).getElementsByTag("img");
    List<String> srcList = imgList.eachAttr("src");
    for (FileEntry imageFile : fileNameList) {
      boolean imageFileUsed = false;
      for (String src : srcList) {
        String srcDecoded = URLUtils.basicUrlDecode(src);
        if (srcDecoded.contains(imageFile.getName())) {
          imageFileUsed = true;
        }
      }
      if (!imageFileUsed) {
        fileSystemService.removeFile(
            customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME + imageFile.getName());
      }
    }
  }

  @Override
  public void deletePreLoginNotice() {
    checkPermissions();
    CustomisationFile customisationFile = new CustomisationFile();
    fileSystemService.removeFile(customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME);
    configurationService.deleteProperty(PRE_LOGIN_NOTICE_KEY);
  }

  public boolean isActive(PreLoginNotice preLoginNotice) {
    switch (preLoginNotice.getScheduleSettings()) {
      case OFF:
        return false;
      case ON:
        return true;
      case SCHEDULED:
        return validateDates(preLoginNotice.getStartDate(), preLoginNotice.getEndDate());
      default:
        return false;
    }
  }

  @Override
  public String uploadPreLoginNoticeImage(InputStream imageFile, String name) throws IOException {
    checkPermissions();
    CustomisationFile customisationFile = new CustomisationFile();
    String nameToUse = iterateImageNameIfDuplicateExists(name);
    fileSystemService.write(
        customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME + nameToUse, imageFile, false);
    return nameToUse;
  }

  private String iterateImageNameIfDuplicateExists(String name) {
    CustomisationFile customisationFile = new CustomisationFile();
    String nameWithoutExtension = FilenameUtils.removeExtension(name);
    String extension = '.' + FilenameUtils.getExtension(name);
    if (fileSystemService.fileExists(
        customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME + nameWithoutExtension + extension)) {
      int i = 1;
      while (fileSystemService.fileExists(
          customisationFile,
          LOGIN_NOTICE_IMAGE_FOLDER_NAME + nameWithoutExtension + '_' + i + extension)) {
        i++;
      }
      return nameWithoutExtension + '_' + i + extension;
    }
    return nameWithoutExtension + extension;
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

  public void checkPermissions() {
    if (tleAclManager
        .filterNonGrantedPrivileges(Collections.singleton(PERMISSION_KEY), false)
        .isEmpty()) {
      throw new PrivilegeRequiredException(PERMISSION_KEY);
    }
  }
}
