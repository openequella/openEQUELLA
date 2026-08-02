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

package com.tle.core.settings.loginnotice.impl;

import static com.tle.legacy.LegacyGuice.loginNoticeEditorPrivilegeTreeProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.core.settings.loginnotice.StoredImage;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.PrivilegeRequiredException;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Bind(LoginNoticeService.class)
public class LoginNoticeServiceImpl implements LoginNoticeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoginNoticeServiceImpl.class);

  private final ConfigurationService configurationService;

  /** Where the images a pre-login notice references are kept. */
  private final LoginNoticeImageStore imageStore;

  /** Whether an uploaded file is fit to be stored as an image. */
  private final LoginNoticeImageValidator imageValidator = new LoginNoticeImageValidator();

  private ObjectMapper objectMapper;

  private static final String PERMISSION_KEY = "EDIT_SYSTEM_SETTINGS";

  private static final String PRE_LOGIN_NOTICE_KEY = "pre.login.notice";

  private static final String POST_LOGIN_NOTICE_KEY = "post.login.notice";

  @Inject
  public LoginNoticeServiceImpl(
      ConfigurationService configurationService, LoginNoticeImageStore imageStore) {
    this.configurationService = configurationService;
    this.imageStore = imageStore;
  }

  @Inject
  protected void setObjectMapperService(ObjectMapperService objectMapperService) {
    objectMapper = objectMapperService.createObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

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
    imageStore.removeUnusedImages(notice.getNotice());
  }

  private boolean validateDates(ZonedDateTime start, ZonedDateTime end) {
    ZonedDateTime now = ZonedDateTime.now(start.getZone());
    return (!now.isBefore(start)) && (now.isBefore(end));
  }

  @Override
  public void deletePreLoginNotice() {
    checkPermissions();
    imageStore.deleteAll();
    configurationService.deleteProperty(PRE_LOGIN_NOTICE_KEY);
  }

  public boolean isActive(PreLoginNotice preLoginNotice) {
    return switch (preLoginNotice.getScheduleSettings()) {
      case OFF -> false;
      case ON -> true;
      case SCHEDULED -> validateDates(preLoginNotice.getStartDate(), preLoginNotice.getEndDate());
      default -> false;
    };
  }

  @Override
  public String uploadPreLoginNoticeImage(InputStream imageFile, String name) throws IOException {
    checkPermissions();
    imageValidator.validateImageExtension(name);
    byte[] imageBytes = IOUtils.toByteArray(imageFile);
    imageValidator.validateImageContent(imageBytes);
    return imageStore.save(imageBytes, name);
  }

  @Override
  public Optional<StoredImage> getPreLoginNoticeImage(String name) throws IOException {
    Optional<String> mimeType = getValidImageMimeType(name);

    if (mimeType.isEmpty()) {
      return Optional.empty();
    }

    return imageStore.read(name).map(content -> new StoredImage(content, mimeType.get()));
  }

  /**
   * The type the stored image may be served as, empty when there is nothing under {@code name} to
   * serve as an image - either no file at all, or one whose content we do not accept.
   */
  private Optional<String> getValidImageMimeType(String name) throws IOException {
    Optional<InputStream> storedImage = imageStore.read(name);
    if (storedImage.isEmpty()) {
      return Optional.empty();
    }

    try (InputStream imageFile = storedImage.get()) {
      Optional<String> mimeType = imageValidator.validMimeTypeOf(imageFile);
      if (mimeType.isEmpty()) {
        LOGGER.warn(
            "Stored login notice image is not a format we serve, treating as absent: {}", name);
      }
      return mimeType;
    }
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
    if (!loginNoticeEditorPrivilegeTreeProvider.isAuthorised()) {
      throw new PrivilegeRequiredException(PERMISSION_KEY);
    }
  }
}
