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

package com.tle.core.newentity.convert;

import com.fasterxml.jackson.databind.JsonNode;
import com.tle.beans.Institution;
import com.tle.beans.newentity.Entity;
import com.tle.common.NameValue;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.CoreStrings;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.service.AbstractJsonConverter;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;
import com.tle.core.newentity.service.EntityService;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class NewEntityConverter extends AbstractJsonConverter<Object> {
  @Inject private EntityService entityService;
  private static final String EXPORT_FOLDER = "entity";
  public static final String ID = "new_entities";

  @Override
  public void doDelete(Institution institution, ConverterParams callback) {
    entityService.deleteAllEntity(institution);
  }

  @Override
  public void doExport(
      TemporaryFileHandle staging, Institution institution, ConverterParams callback)
      throws IOException {
    for (Entity entity : entityService.getAllEntity(institution)) {
      String uuid = entity.id().uuid();
      String typeId = entity.typeid();

      EntityExport entityExport = new EntityExport();
      entityExport.uuid = uuid;
      entityExport.typeId = typeId;
      entityExport.name = entity.name();
      entityExport.nameStrings = entity.nameStrings();
      entityExport.description = entity.description();
      entityExport.descriptionStrings = entity.descriptionStrings();
      entityExport.created = entity.created().toEpochMilli();
      entityExport.modified = entity.modified().toEpochMilli();
      entityExport.data = json.getMapper().readTree(entity.data());
      entityExport.owner = entity.owner();

      json.write(
          new SubTemporaryFile(staging, EXPORT_FOLDER + "/" + typeId),
          uuid + ".json",
          entityExport);
    }
  }

  @Override
  public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
      throws IOException {
    final SubTemporaryFile viewsImportFolder = new SubTemporaryFile(staging, EXPORT_FOLDER);
    final List<String> entries = json.getFileList(viewsImportFolder);

    for (String entry : entries) {
      Optional.ofNullable(json.read(viewsImportFolder, entry, EntityExport.class))
          .map(
              entityExport -> {
                entityService.setEntity(
                    entityExport.uuid,
                    entityExport.typeId,
                    entityExport.name,
                    entityExport.nameStrings,
                    entityExport.description,
                    entityExport.descriptionStrings,
                    Instant.ofEpochMilli(entityExport.created),
                    Instant.ofEpochMilli(entityExport.modified),
                    entityExport.owner,
                    entityExport.data.toString(),
                    institution);

                return null;
              });
    }
  }

  @Override
  public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params) {
    tasks.add(new NameValue(CoreStrings.text("impexp.entities"), ID));
  }

  public static class EntityExport {
    public String uuid;
    public String typeId;
    public String name;
    public String nameStrings;
    public String description;
    public String descriptionStrings;
    public String owner;
    public long created;
    public long modified;
    public JsonNode data;
  }
}
