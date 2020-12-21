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

package com.tle.core.facetedsearch.converter;

import com.tle.beans.Institution;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.facetedsearch.bean.FacetedSearchClassification;
import com.tle.core.facetedsearch.converter.FacetedSearchClassificationConverter.FacetedSearchClassificationConverterInfo;
import com.tle.core.facetedsearch.dao.FacetedSearchClassificationDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.PostReadMigrator;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class FacetedSearchClassificationConverter
    extends AbstractConverter<FacetedSearchClassificationConverterInfo> {
  private static final String BACKUP_FILE = "faceted_search/classifications.xml";
  private static final String PLUGIN_ID = "FACETEDSEARCHCLASSIFICATION";
  @Inject private FacetedSearchClassificationDao facetedSearchClassificationDao;

  @Override
  public void doExport(
      TemporaryFileHandle staging, Institution institution, ConverterParams callback)
      throws IOException {
    xmlHelper.writeXmlFile(staging, BACKUP_FILE, facetedSearchClassificationDao.enumerateAll());
  }

  @Override
  public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
      throws IOException {
    if (!fileSystemService.fileExists(staging, BACKUP_FILE)) {
      return;
    }
    final List<FacetedSearchClassification> classifications =
        xmlHelper.readXmlFile(staging, BACKUP_FILE);
    for (FacetedSearchClassification classification : classifications) {
      classification.setInstitution(institution);
    }
    final Collection<PostReadMigrator<FacetedSearchClassificationConverterInfo>> migrations =
        getMigrations(params);
    runMigrations(
        migrations, new FacetedSearchClassificationConverterInfo(classifications, params));

    for (FacetedSearchClassification classification : classifications) {
      facetedSearchClassificationDao.save(classification);
      facetedSearchClassificationDao.flush();
      facetedSearchClassificationDao.clear();
    }
  }

  @Override
  public void doDelete(Institution institution, ConverterParams params) {
    for (FacetedSearchClassification classification :
        facetedSearchClassificationDao.enumerateAll()) {
      facetedSearchClassificationDao.delete(classification);
    }
  }

  @Override
  public String getStringId() {
    return PLUGIN_ID;
  }

  public static class FacetedSearchClassificationConverterInfo {
    private final List<FacetedSearchClassification> prefs;
    private final ConverterParams params;

    public FacetedSearchClassificationConverterInfo(
        List<FacetedSearchClassification> prefs, ConverterParams params) {
      this.prefs = prefs;
      this.params = params;
    }

    public ConverterParams getParams() {
      return params;
    }

    public List<FacetedSearchClassification> getPrefs() {
      return prefs;
    }
  }
}
