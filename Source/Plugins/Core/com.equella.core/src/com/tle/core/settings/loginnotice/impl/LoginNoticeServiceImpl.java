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
import com.tle.common.Check;
import com.tle.common.filesystem.FileEntry;
import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.PrivilegeRequiredException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

@Singleton
@Bind(LoginNoticeService.class)
public class LoginNoticeServiceImpl implements LoginNoticeService {
  @Inject TLEAclManager tleAclManager;
  @Inject ConfigurationService configurationService;
  @Inject FileSystemService fileSystemService;

  @Inject
  protected void setObjectMapperService(ObjectMapperService objectMapperService) {
    objectMapper = objectMapperService.createObjectMapper();
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
      if (notice.getEndDate().toInstant().isBefore(notice.getStartDate().toInstant())) {
        throw new BadRequestException(
            "Invalid start and end date. Start date must be on or before end date.");
      }
      configurationService.setProperty(
          PRE_LOGIN_NOTICE_KEY, objectMapper.writeValueAsString(notice));
    }
    cleanUpUnusedImages(notice.getNotice());
  }

  private boolean validateDates(Date start, Date end) {
    Calendar now = Calendar.getInstance();
    Calendar startDate = Calendar.getInstance();
    Calendar endDate = Calendar.getInstance();
    startDate.setTime(start);
    endDate.setTime(end);
    startDate = getDateAtMidnight(startDate);
    endDate = getDateAtMidnight(endDate);
    endDate.add(Calendar.DAY_OF_YEAR, 1);
    return !(now.after(endDate) || now.before(startDate));
  }

  private void cleanUpUnusedImages(String notice) throws IOException {
    CustomisationFile customisationFile = new CustomisationFile();
    FileEntry[] fileNameList =
        fileSystemService.enumerate(customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME, null);
    for (FileEntry imageFile : fileNameList) {
      if (!notice.contains(imageFile.getName())) {
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

  private Calendar getDateAtMidnight(Calendar date) {
    Calendar dateToReturn = (Calendar) date.clone();
    dateToReturn.set(Calendar.HOUR, 0);
    dateToReturn.set(Calendar.MINUTE, 0);
    dateToReturn.set(Calendar.SECOND, 0);
    dateToReturn.set(Calendar.MILLISECOND, 0);
    return dateToReturn;
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
  public String uploadPreLoginNoticeImage(File imageFile, String name) throws IOException {
    checkPermissions();
    CustomisationFile customisationFile = new CustomisationFile();

    // read in image file
    BufferedImage bImage = null;
    bImage = ImageIO.read(imageFile);
    if (bImage == null) {
      throw new IllegalArgumentException("Invalid image file");
    }
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(bImage, "png", os);
    InputStream fis = new ByteArrayInputStream(os.toByteArray());
    String nameToUse = iterateImageNameIfDuplicateExists(name, ".png");

    fileSystemService.write(
        customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME + nameToUse, fis, false);
    os.close();
    fis.close();
    return nameToUse;
  }

  private String iterateImageNameIfDuplicateExists(String name, String newFileExtension) {
    CustomisationFile customisationFile = new CustomisationFile();
    String nameWithoutExtension = FilenameUtils.removeExtension(name);
    if (fileSystemService.fileExists(
        customisationFile,
        LOGIN_NOTICE_IMAGE_FOLDER_NAME + nameWithoutExtension + newFileExtension)) {
      int i = 1;
      while (fileSystemService.fileExists(
          customisationFile,
          LOGIN_NOTICE_IMAGE_FOLDER_NAME + nameWithoutExtension + '_' + i + newFileExtension)) {
        i++;
      }
      return nameWithoutExtension + '_' + i + newFileExtension;
    }
    return nameWithoutExtension + newFileExtension;
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
