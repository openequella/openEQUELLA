package com.tle.core.settings.service.impl;

import io.bit3.jsass.importer.Import;
import io.bit3.jsass.importer.Importer;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyCssImporter implements Importer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThemeSettingsServiceImpl.class);

  @Override
  public Collection<Import> apply(String url, Import previous) {
    final String BASE_CSS_PATH = "/web/css/";
    URI legacyScss = null;

    try {
      legacyScss = getClass().getResource(BASE_CSS_PATH + url).toURI();

      return Collections.singleton(
          new Import(
              legacyScss,
              legacyScss,
              IOUtils.toString(getClass().getResource(BASE_CSS_PATH + url), "UTF-8")));
    } catch (Exception e) {
      LOGGER.error("Failed to import legacy css", e);
      return null;
    }
  }
}
