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

package com.tle.core.schema.dao.impl;

import com.tle.beans.entity.Schema;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.schema.dao.SchemaDao;
import java.util.List;
import javax.inject.Singleton;

@Bind(SchemaDao.class)
@Singleton
@SuppressWarnings("nls")
public class SchemaDaoImpl extends AbstractEntityDaoImpl<Schema> implements SchemaDao {
  public SchemaDaoImpl() {
    super(Schema.class);
  }

  @Override
  public List<String> getExportSchemaTypes() {
    return (List<String>)
        getHibernateTemplate()
            .find(
                "SELECT DISTINCT t.type FROM Schema s INNER JOIN s.expTransforms AS t WHERE"
                    + " s.institution = ?0 ORDER BY t.type",
                CurrentInstitution.get());
  }

  @Override
  public List<String> getImportSchemaTypes(long id) {
    return (List<String>)
        getHibernateTemplate()
            .find(
                "select distinct t.type from Schema s inner join s.impTransforms as t where s.id ="
                    + " ?0 order by t.type",
                id);
  }

  @Override
  public List<Schema> getSchemasForExportSchemaType(String type) {
    return (List<Schema>)
        getHibernateTemplate()
            .find(
                "SELECT s FROM Schema s INNER JOIN s.expTransforms t WHERE s.institution = ?0 AND"
                    + " LOWER(t.type) = ?1",
                new Object[] {CurrentInstitution.get(), type.toLowerCase()});
  }

  @Override
  @SuppressWarnings("nls")
  public List<String> getAllCitations() {
    return (List<String>)
        getHibernateTemplate()
            .findByNamedParam(
                "select distinct c.name from Schema s join s.citations c where s.institution ="
                    + " :inst",
                "inst",
                CurrentInstitution.get());
  }
}
