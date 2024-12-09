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

package com.tle.core.institution.convert;

import com.dytech.edge.common.Constants;
import com.tle.beans.Institution;
import com.tle.common.filesystem.handle.ExportFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;
import com.tle.core.util.archive.ArchiveProgress;
import com.tle.core.util.archive.ArchiveType;
import java.io.IOException;
import javax.inject.Singleton;

@Bind
@Singleton
public class ZippingConverter extends AbstractConverter<Object> {
  public static String ID = "ZIPFILES";

  @Override
  public void doDelete(Institution institution, ConverterParams callback) {
    // nada
  }

  @Override
  public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
      throws IOException {
    // now zip it
    DefaultMessageCallback message =
        new DefaultMessageCallback("institutions.converter.filestore.zipping"); // $NON-NLS-1$
    params.setMessageCallback(message);

    int numFiles = fileSystemService.grep(staging, Constants.BLANK, "**").size(); // $NON-NLS-1$
    message.setTotal(numFiles);

    fileSystemService.zipFile(
        staging,
        Constants.BLANK,
        new ExportFile(staging.getMyPathComponent() + ".tgz"), // $NON-NLS-1$
        Constants.BLANK,
        ArchiveType.TAR_GZ,
        new ZippingProgress(message));
  }

  @Override
  public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
      throws IOException {
    // nada
  }

  @Override
  public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params) {
    if (type == ConvertType.EXPORT) {
      tasks.add(getStandardTask(ID));
    }
  }

  public static class ZippingProgress extends ArchiveProgress {
    private final DefaultMessageCallback progress;

    public ZippingProgress(final DefaultMessageCallback progress) {
      super(progress.getTotal());
      this.progress = progress;
    }

    @Override
    public void nextEntry(String entryPath) {
      progress.incrementCurrent();
    }

    @Override
    public void setCallbackMessageValue(String message) {
      progress.setType(message);
    }

    @Override
    public void incrementWarningCount() {
      long oldTotal = progress.getTotal();
      progress.setTotal(++oldTotal);
    }

    public DefaultMessageCallback getProgress() {
      return progress;
    }
  }
}
