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

package com.tle.core.schema.service;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.Schema;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;
import java.util.List;
import java.util.Set;

public interface SchemaService extends AbstractEntityService<EntityEditingBean, Schema> {
  String ENTITY_TYPE = "SCHEMA";

  List<String> getExportSchemaTypes();

  Set<Schema> getSchemasForExportSchemaType(String type);

  // Need this for OAI, otherwise run out of connections...
  String transformForExport(long id, String type, PropBagEx itemxml, boolean omitXmlDeclaration);

  String transformForImport(long id, String type, PropBagEx foreignXml);

  List<String> getAllCitations();

  /**
   * Get the uses of a metadata schema by ID.
   *
   * <p>Returns a list of entities (items, collections, etc.) that reference or use this metadata
   * schema. This is useful for understanding the impact of schema changes and for managing
   * dependencies.
   *
   * @param id the ID of the metadata schema
   * @return a list of entities that use this schema
   */
  List<BaseEntityLabel> getSchemaUses(long id);

  /**
   * Get the types of schema import transformations for a metadata schema by ID.
   *
   * <p>Returns the types of transformations that can be applied when importing this schema between
   * repositories. Import transformations are used to adapt schema structure and data format when
   * moving content from one system to another.
   *
   * @param id the ID of the metadata schema
   * @return a list of import transformation type names
   */
  List<String> getImportSchemaTypes(long id);
}
