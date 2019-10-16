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

package com.tle.core.settings.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.settings.service.ThemeSettingsService;
import com.tle.exceptions.PrivilegeRequiredException;
import com.tle.web.api.newuitheme.impl.NewUITheme;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@SuppressWarnings("nls")
@Bind(ThemeSettingsService.class)
public class ThemeSettingsServiceImpl implements ThemeSettingsService {
  @Inject TLEAclManager tleAclManager;
  @Inject ConfigurationService configurationService;
  @Inject FileSystemService fileSystemService;
  @Inject private StagingService stagingService;

  private static final Logger LOGGER = LoggerFactory.getLogger(ThemeSettingsServiceImpl.class);

  @Inject
  protected void setObjectMapperService(ObjectMapperService objectMapperService) {
    objectMapper = objectMapperService.createObjectMapper();
  }

  private ObjectMapper objectMapper;

  private static final String PERMISSION_KEY = "EDIT_SYSTEM_SETTINGS";
  private static final String LOGO_FILENAME = "newLogo.png";
  private static final String THEME_KEY = "Theme";
  private static final String SASS_VARS_FILENAME = "variables.scss";
  private static final String SASS_LEGACY_CSS_FILENAME = "legacy.scss";
  private static final String LEGACY_CSS_FILENAME = "legacy.css";

  @Override
  public NewUITheme getTheme() throws IOException {
    String themeString = configurationService.getProperty(THEME_KEY);
    // use default theme if none exists in database
    return Check.isEmpty(themeString)
        ? new NewUITheme()
        : objectMapper.readValue(themeString, NewUITheme.class);
  }

  @Override
  public InputStream getCustomLogo() throws IOException {
    CustomisationFile customisationFile = new CustomisationFile();
    if (fileSystemService.fileExists(customisationFile, LOGO_FILENAME)) {
      return fileSystemService.read(customisationFile, LOGO_FILENAME);
    }
    return null;
  }

  @Override
  public boolean isCustomLogo() {
    CustomisationFile customisationFile = new CustomisationFile();
    return fileSystemService.fileExists(customisationFile, LOGO_FILENAME);
  }

  @Override
  public void setTheme(NewUITheme theme) throws JsonProcessingException, IOException {
    checkPermissions();
    String themeString = themeToJSONString(theme);
    configurationService.setProperty(THEME_KEY, themeString);
    compileSass();
  }

  @Override
  public void setLogo(File logoFile) throws IOException {
    checkPermissions();
    CustomisationFile customisationFile = new CustomisationFile();
    // read in image file
    BufferedImage bImage = null;
    bImage = ImageIO.read(logoFile);
    if (bImage == null) {
      throw new IllegalArgumentException("Invalid image file");
    }

    // resize image to logo size (230px x 36px)
    BufferedImage resizedImage = new BufferedImage(230, 36, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = (Graphics2D) resizedImage.getGraphics();
    g2d.drawImage(
        bImage,
        0,
        0,
        resizedImage.getWidth() - 1,
        resizedImage.getHeight() - 1,
        0,
        0,
        bImage.getWidth() - 1,
        bImage.getHeight() - 1,
        null);
    g2d.dispose();
    RenderedImage rImage = resizedImage;

    // write resized image to image file in the institution's filestore
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(rImage, "png", os);
    InputStream fis = new ByteArrayInputStream(os.toByteArray());
    fileSystemService.write(customisationFile, LOGO_FILENAME, fis, false);
    os.close();
    fis.close();
  }

  @Override
  public void deleteLogo() {
    checkPermissions();
    CustomisationFile customisationFile = new CustomisationFile();
    fileSystemService.removeFile(customisationFile, LOGO_FILENAME);
  }

  private void checkPermissions() {
    if (tleAclManager
        .filterNonGrantedPrivileges(Collections.singleton(PERMISSION_KEY), false)
        .isEmpty()) {
      throw new PrivilegeRequiredException(PERMISSION_KEY);
    }
  }

  private String themeToJSONString(NewUITheme theme) throws JsonProcessingException {
    String themeToString = "";
    themeToString = objectMapper.writeValueAsString(theme);
    return themeToString;
  }

  public InputStream getLegacyCss() throws IOException {
    File baseLegacySass =
        new File(getClass().getResource("/web/sass/" + SASS_LEGACY_CSS_FILENAME).getFile());
    CustomisationFile customisationFile = new CustomisationFile();
    boolean legacyCssExists = fileSystemService.fileExists(customisationFile, LEGACY_CSS_FILENAME);
    boolean baseSassUpdated =
        baseLegacySass.lastModified()
            > fileSystemService.lastModified(customisationFile, LEGACY_CSS_FILENAME);

    if (!legacyCssExists || baseSassUpdated) {
      compileSass();
    }
    return fileSystemService.read(customisationFile, LEGACY_CSS_FILENAME);
  }

  private InputStream compileSass() throws IOException {
    CustomisationFile customisationFile = new CustomisationFile();
    StagingFile staging = stagingService.createStagingArea();

    InputStream legacyScss =
        getClass().getResourceAsStream("/web/sass/" + SASS_LEGACY_CSS_FILENAME);

    Compiler compiler = new Compiler();
    Options options = new Options();

    options.getIncludePaths().add(new File(getClass().getResource("/web/css/").getFile()));

    fileSystemService.write(staging, SASS_LEGACY_CSS_FILENAME, legacyScss, false);
    fileSystemService.write(
        staging, SASS_VARS_FILENAME, new StringReader(getTheme().toSassVars()), false);

    final File dstFile = fileSystemService.getExternalFile(staging, LEGACY_CSS_FILENAME);

    try {
      Output output =
          compiler.compileFile(
              fileSystemService.getExternalFile(staging, SASS_LEGACY_CSS_FILENAME).toURI(),
              dstFile.toURI(),
              options);
      fileSystemService.write(
          customisationFile, LEGACY_CSS_FILENAME, new StringReader(output.getCss()), false);
    } catch (Exception e) {
      LOGGER.error("Failed to compile Sass to css " + e);
    }
    return legacyScss;
  }
}
