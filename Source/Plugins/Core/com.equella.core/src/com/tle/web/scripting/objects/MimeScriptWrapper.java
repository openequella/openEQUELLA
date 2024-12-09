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

package com.tle.web.scripting.objects;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.scripting.objects.MimeScriptObject;
import com.tle.common.scripting.types.MimeTypeScriptType;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind(MimeScriptObject.class)
@Singleton
public class MimeScriptWrapper extends AbstractScriptWrapper implements MimeScriptObject {
  private static final long serialVersionUID = 1L;

  @Inject private MimeTypeService mimeService;

  @Override
  public MimeTypeScriptType getMimeTypeForFilename(String filename) {
    MimeEntry entry = mimeService.getEntryForFilename(filename);
    if (entry != null) {
      return new MimeTypeScriptTypeImpl(entry);
    }
    return null;
  }

  public static class MimeTypeScriptTypeImpl implements MimeTypeScriptType {
    private static final long serialVersionUID = 1L;

    private final MimeEntry wrapped;
    private List<String> otherExtensions;

    public MimeTypeScriptTypeImpl(MimeEntry wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public String getDescription() {
      // null safe it
      String description = wrapped.getDescription();
      return (description == null ? "" : description); // $NON-NLS-1$
    }

    @Override
    public List<String> getFileExtensions() {
      if (otherExtensions == null) {
        otherExtensions = new ArrayList<String>(wrapped.getExtensions());
        Collections.sort(otherExtensions);
      }
      return otherExtensions;
    }

    @Override
    public String getType() {
      return wrapped.getType();
    }
  }
}
