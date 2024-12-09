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

package com.tle.core.usermanagement.standard.dao.impl;

import com.google.inject.Singleton;
import com.tle.beans.user.UserInfoBackup;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.usermanagement.standard.dao.UserInfoBackupDao;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Bind(UserInfoBackupDao.class)
@Singleton
public class UserInfoBackupDaoImpl extends GenericDaoImpl<UserInfoBackup, Long>
    implements UserInfoBackupDao {

  public UserInfoBackupDaoImpl() {
    super(UserInfoBackup.class);
  }

  @Override
  public UserInfoBackup findUserInfoBackup(String uniqueId) {
    return (UserInfoBackup)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) {
                    Query query =
                        session.createQuery(
                            "FROM UserInfoBackup WHERE LOWER(uniqueId) = :uniqueId AND"
                                + " institution_id = :institutionId");
                    query.setParameter("uniqueId", uniqueId.toLowerCase());
                    query.setParameter("institutionId", CurrentInstitution.get().getUniqueId());
                    return query.uniqueResult();
                  }
                });
  }

  @Override
  public List<UserInfoBackup> getAllInfo() {
    return (List<UserInfoBackup>)
        getHibernateTemplate()
            .find(
                "from UserInfoBackup where institution_id = ?0",
                CurrentInstitution.get().getUniqueId());
  }

  @Transactional(propagation = Propagation.MANDATORY)
  @Override
  public void deleteAllInfo() {
    getHibernateTemplate()
        .execute(
            session -> {
              Query query =
                  session.createQuery(
                      "delete from UserInfoBackup where institution_id = :institutionId");
              query.setParameter("institutionId", CurrentInstitution.get().getUniqueId());
              query.executeUpdate();
              return null;
            });
  }
}
