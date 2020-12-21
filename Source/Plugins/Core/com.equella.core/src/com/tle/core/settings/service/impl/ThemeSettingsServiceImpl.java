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
import com.tle.core.services.UrlService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.settings.service.ThemeSettingsService;
import com.tle.exceptions.PrivilegeRequiredException;
import com.tle.web.api.newuitheme.impl.NewUITheme;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.context.StringContext;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLConnection;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
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
  @Inject UrlService url;

  private static final Logger LOGGER = LoggerFactory.getLogger(ThemeSettingsServiceImpl.class);

  @Inject
  protected void setObjectMapperService(ObjectMapperService objectMapperService) {
    objectMapper = objectMapperService.createObjectMapper();
  }

  private ObjectMapper objectMapper;

  private static final String PERMISSION_KEY = "EDIT_SYSTEM_SETTINGS";
  private static final String LOGO_FILENAME = "newLogo.png";
  private static final String THEME_KEY = "Theme";
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
  public void setTheme(NewUITheme theme) throws IOException {
    checkPermissions();
    String themeString = themeToJSONString(theme);
    configurationService.setProperty(THEME_KEY, themeString);
    compileSass();
  }

  @Override
  public void setLogo(File logoFile) throws IOException {
    checkPermissions();

    // Resize logo
    BufferedImage logo = ImageIO.read(logoFile);
    if (logo == null) {
      throw new IllegalArgumentException("Invalid image file");
    }
    RenderedImage resizedLogo = resizeLogo(logo);

    // write resized logo to image file in the institution's filestore
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(resizedLogo, "png", os);
    InputStream fis = new ByteArrayInputStream(os.toByteArray());
    fileSystemService.write(new CustomisationFile(), LOGO_FILENAME, fis, false);
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
    CustomisationFile customisationFile = new CustomisationFile();
    boolean needsUpdate = false;

    // compare against the modified date of the css file if it exists
    boolean legacyCssExists = fileSystemService.fileExists(customisationFile, LEGACY_CSS_FILENAME);
    if (legacyCssExists) {
      // get last modified date of the scss file
      URLConnection conn =
          getClass().getResource("/web/sass/" + SASS_LEGACY_CSS_FILENAME).openConnection();
      long lastMod = conn.getLastModified();
      conn.getInputStream().close();

      // get last modified of the compiled css file
      long cssLastMod = fileSystemService.lastModified(customisationFile, LEGACY_CSS_FILENAME);
      if (lastMod > cssLastMod) {
        needsUpdate = true;
      }
    } else {
      needsUpdate = true;
    }

    if (needsUpdate) {
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
    final File dstFile = fileSystemService.getExternalFile(staging, LEGACY_CSS_FILENAME);

    options.getImporters().add(new LegacyCssImporter());

    StringContext fileContext =
        new StringContext(
            getTheme().toSassVars() + IOUtils.toString(legacyScss), null, dstFile.toURI(), options);

    try {
      Output output = compiler.compile(fileContext);
      fileSystemService.write(
          customisationFile, LEGACY_CSS_FILENAME, new StringReader(output.getCss()), false);
    } catch (Exception e) {
      LOGGER.debug("Failed to compile Sass to css ", e);
    }
    return legacyScss;
  }

  private BufferedImage resizeLogo(BufferedImage logo) {
    if (logo == null) {
      throw new IllegalArgumentException("resizeLogo() should not be called with a null logo.");
    }

    final int maxWidth = 230; // based on New UI layout
    final int width = Math.min(maxWidth, logo.getWidth());
    final float scale = (float) width / logo.getWidth();
    final int height = (int) (logo.getHeight() * scale);

    BufferedImage resizedLogo = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = (Graphics2D) resizedLogo.getGraphics();
    g2d.drawImage(
        logo,
        0,
        0,
        resizedLogo.getWidth() - 1,
        resizedLogo.getHeight() - 1,
        0,
        0,
        logo.getWidth() - 1,
        logo.getHeight() - 1,
        null);
    g2d.dispose();

    return resizedLogo;
  }
}
