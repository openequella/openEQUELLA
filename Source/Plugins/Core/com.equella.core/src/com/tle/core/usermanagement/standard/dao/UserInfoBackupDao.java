package com.tle.core.usermanagement.standard.dao;

import com.tle.beans.user.UserInfoBackup;
import com.tle.core.hibernate.dao.GenericDao;

public interface UserInfoBackupDao extends GenericDao<UserInfoBackup, Long> {
  UserInfoBackup findUserInfoBackup(String username);
}
