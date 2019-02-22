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

package com.tle.web.service.oai;

import ORG.oclc.oai.server.verb.IdDoesNotExistException;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.settings.standard.OAISettings;
import com.tle.core.institution.InstitutionService;
import com.tle.core.settings.service.ConfigurationService;
import java.util.concurrent.TimeUnit;

public final class OAIUtils {
  private static long cachedTime = 0;
  private static OAIUtils cachedUtils;

  public static synchronized OAIUtils getInstance(
      InstitutionService institutionService, ConfigurationService configService) {
    final long now = System.currentTimeMillis();
    if (now > cachedTime + TimeUnit.MINUTES.toMillis(1)) {
      cachedTime = now;
      cachedUtils =
          new OAIUtils(institutionService, configService.getProperties(new OAISettings()));
    }
    return cachedUtils;
  }

  // // OBJECT INSTANCE STUFF BELOW /////////////////////////////////////////

  private final OAISettings settings;

  // Cached
  private transient String namespaceIdentifier;
  private transient String schemaPlusNamespace;

  private OAIUtils(final InstitutionService institutionService, final OAISettings settings) {
    this.settings = settings;

    namespaceIdentifier = settings.getNamespaceIdentifier();
    if (Check.isEmpty(namespaceIdentifier)) {
      namespaceIdentifier = institutionService.getInstitutionUrl().getHost();
    }

    schemaPlusNamespace = settings.getScheme() + ':' + namespaceIdentifier + ':';
  }

  public String getScheme() {
    return settings.getScheme();
  }

  public String getNamespaceIdentifier() {
    return namespaceIdentifier;
  }

  public String getIdentifier(final ItemId itemId) {
    return schemaPlusNamespace + itemId.toString();
  }

  public String getSampleIdentifier() {
    return getIdentifier(new ItemId("ABCDEF", 1)); // $NON-NLS-1$
  }

  public boolean isUseDownloadItemAcl() {
    return settings.isUseDownloadItemAcl();
  }

  public ItemId parseRecordIdentifier(final String id) throws IdDoesNotExistException {
    if (!id.startsWith(schemaPlusNamespace)) {
      throw new IdDoesNotExistException(id);
    } else {
      return new ItemId(id.substring(schemaPlusNamespace.length()));
    }
  }
}
