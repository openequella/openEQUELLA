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

package com.tle.core.freetext.extracter.standard;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class Office2007DocxExtracter extends AbstractOffice2007Extracter {
  @Override
  public boolean isSupportedByDefault(MimeEntry mimeEntry) {
    return mimeEntry
        .getType()
        .startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.");
  }

  @Override
  public String getFileToIndex() {
    return "word/document.xml";
  }

  @Override
  public String getNameOfElementToIndex() {
    return "w:t";
  }

  @Override
  public boolean isMimeTypeSupported(String mimeType) {
    return mimeType.toLowerCase().contains("wordprocessingml");
  }

  @Override
  public boolean multipleFiles() {
    return false;
  }
}
