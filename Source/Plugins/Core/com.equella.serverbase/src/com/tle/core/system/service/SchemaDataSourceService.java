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

package com.tle.core.system.service;

import com.tle.beans.DatabaseSchema;
import com.tle.core.hibernate.DataSourceHolder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface SchemaDataSourceService {
  DataSourceHolder getDataSourceForId(long schemaId);

  DataSourceHolder getReportingDataSourceForId(long schemaId);

  void removeFromCache(long schemaId);

  <V> V executeWithSchema(long schemaId, Callable<V> code);

  <V> Future<V> executeWithSchema(ExecutorService executor, long schemaId, Callable<V> code);

  /**
   * Return connection details, taking into account the useSystem flag.
   *
   * @param schema
   * @return Element[0] = url, Element[1] = username, Element[2] = password
   */
  String[] getConnectionDetails(DatabaseSchema schema);

  void removeSchemaDataSource(long schemaId);
}
