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

package com.tle.mets.metadata;

import static com.tle.mets.MetsConstants.METS_FILENAME;

import com.dytech.edge.ejb.helpers.metadata.mappers.AbstractXPathPackageMapper;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.ims.service.IMSService;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class METSMetadataMapper extends AbstractXPathPackageMapper {
  @Inject private IMSService imsService;

  @Override
  public boolean isSupportedPackage(FileHandle handle, String packageName) {
    try (InputStream inp =
        imsService.getMetsManifestAsStream(handle, packageName, METS_FILENAME, false)) {
      return inp != null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected InputStream getXmlStream(FileHandle handle, String packageName) throws IOException {
    return imsService.getMetsManifestAsStream(handle, packageName, METS_FILENAME, true);
  }

  @Override
  public List<String> getSupportedFormatsForDisplay() {
    return Arrays.asList(
        new String[] {
          CurrentLocale.get("com.tle.mets.metspackage"), // $NON-NLS-1$
          CurrentLocale.get("com.tle.mets.metsmanifest")
        }); //$NON-NLS-1$
  }
}
