package com.tle.core.usermanagement.standard.dao.impl;

import com.google.inject.Singleton;
import com.tle.beans.user.UserInfoBackup;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.usermanagement.standard.dao.UserInfoBackupDao;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

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
                            "FROM UserInfoBackup WHERE LOWER(uniqueId) = :uniqueId AND institution_id = :institutionId");
                    query.setParameter("uniqueId", uniqueId.toLowerCase());
                    query.setParameter("institutionId", CurrentInstitution.get().getUniqueId());
                    return query.uniqueResult();
                  }
                });
  }

  @Override
  public void saveOrUpdate(UserInfoBackup entity) {
    super.saveOrUpdate(entity);
  }
}
