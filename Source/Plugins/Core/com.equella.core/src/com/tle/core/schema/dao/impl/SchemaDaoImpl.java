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
import java.util.function.Consumer;
import javax.inject.Singleton;
import org.hibernate.query.Query;

@Bind(SchemaDao.class)
@Singleton
public class SchemaDaoImpl extends AbstractEntityDaoImpl<Schema> implements SchemaDao {
  public SchemaDaoImpl() {
    super(Schema.class);
  }

  @Override
  public List<String> getExportSchemaTypes() {
    return querySchemasOfCurrentInstitution(
        "SELECT DISTINCT t.type FROM Schema s INNER JOIN s.expTransforms AS t"
            + " WHERE s.institution = :institution ORDER BY t.type",
        String.class);
  }

  @Override
  public List<String> getImportSchemaTypes(long id) {
    return querySchemasOfCurrentInstitution(
        "SELECT DISTINCT t.type FROM Schema s INNER JOIN s.impTransforms AS t"
            + " WHERE s.institution = :institution AND s.id = :id ORDER BY t.type",
        String.class,
        query -> query.setParameter("id", id));
  }

  @Override
  public List<Schema> getSchemasForExportSchemaType(String type) {
    return querySchemasOfCurrentInstitution(
        "SELECT s FROM Schema s INNER JOIN s.expTransforms t"
            + " WHERE s.institution = :institution AND LOWER(t.type) = :type",
        Schema.class,
        query -> query.setParameter("type", type.toLowerCase()));
  }

  @Override
  public List<String> getAllCitations() {
    return querySchemasOfCurrentInstitution(
        "SELECT DISTINCT c.name FROM Schema s INNER JOIN s.citations c"
            + " WHERE s.institution = :institution",
        String.class);
  }

  /** Runs a query which needs no parameters beyond {@code :institution}. */
  private <R> List<R> querySchemasOfCurrentInstitution(String hql, Class<R> resultType) {
    return querySchemasOfCurrentInstitution(hql, resultType, query -> {});
  }

  /**
   * Runs an HQL query over the schemas of the current institution.
   *
   * <p>Every query in this DAO joins one of {@code Schema}'s collections and filters by
   * institution, so the {@code :institution} binding - and the typing which avoids an unchecked
   * cast on the result - are done here rather than four times over.
   *
   * @param hql the query, which must declare an {@code :institution} parameter
   * @param resultType the type each row is projected to
   * @param bindRemainingParameters binds whatever else the query declares
   * @param <R> the type each row is projected to
   * @return the matching rows
   */
  private <R> List<R> querySchemasOfCurrentInstitution(
      String hql, Class<R> resultType, Consumer<Query<R>> bindRemainingParameters) {
    return withSession(
        session -> {
          Query<R> query =
              session
                  .createQuery(hql, resultType)
                  .setParameter("institution", CurrentInstitution.get());
          bindRemainingParameters.accept(query);

          return query.getResultList();
        });
  }
}
