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

package com.tle.core.hibernate.dao;

import com.tle.common.institution.CurrentInstitution;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

/**
 * A builder for creating a query to count the number of associations between two entities. For
 * example, to count the number of items in a collection of items - users in a group.
 *
 * @param <T> The type of the entity to count associations for
 */
public class AssociationCountQueryBuilder<T> {
  private final EntityManager entityManager;
  private Class<T> entityClass;
  private String associationAttribute;
  private String idAttribute;
  private String id;
  boolean targetIdOnAssociation = false;

  public AssociationCountQueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Set the entity class to count associations for.
   *
   * @param entityClass The entity class to count associations for
   * @return This builder
   */
  public AssociationCountQueryBuilder<T> forEntity(Class<T> entityClass) {
    this.entityClass = entityClass;
    return this;
  }

  /**
   * Set the association attribute to count associations for.
   *
   * @param attributeName The attribute name specifying the associations which are to be counted.
   * @return This builder
   */
  public AssociationCountQueryBuilder<T> forAssociation(String attributeName) {
    this.associationAttribute = attributeName;
    return this;
  }

  /**
   * Set the id attribute to count associations for. As an example, if the entity is a group and the
   * association is a user, the id attribute would be the group id.
   *
   * @param idAttribute The attribute name specifying the id of the entity to count associations for
   * @param id The id of the entity to count associations for
   * @return This builder
   */
  public AssociationCountQueryBuilder<T> withId(String idAttribute, String id) {
    this.idAttribute = idAttribute;
    this.id = id;
    return this;
  }

  /**
   * Use the provided ID to match against association entity. This is useful when the association is
   * a join table between two entities and the id is on the association entity.
   *
   * @return This builder
   */
  public AssociationCountQueryBuilder<T> targetIdOnAssociation() {
    this.targetIdOnAssociation = true;
    return this;
  }

  /**
   * Build the query to count the number of associations.
   *
   * @return The query to count the number of associations
   */
  public TypedQuery<Long> build() {
    checkInitialised();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

    Root<T> root = criteriaQuery.from(entityClass);
    From<?, T> targetEntity = targetIdOnAssociation ? root.join(associationAttribute) : root;

    criteriaQuery.select(criteriaBuilder.count(root.join(associationAttribute)));
    criteriaQuery.where(
        criteriaBuilder.equal(targetEntity.get(idAttribute), id),
        criteriaBuilder.equal(targetEntity.get("institution"), CurrentInstitution.get()));

    return entityManager.createQuery(criteriaQuery);
  }

  private void checkInitialised() {
    if (entityClass == null
        || StringUtils.isBlank(associationAttribute)
        || StringUtils.isBlank(idAttribute)) {
      throw new IllegalStateException("AssociationCountQueryBuilder not fully initialised");
    }
  }
}
