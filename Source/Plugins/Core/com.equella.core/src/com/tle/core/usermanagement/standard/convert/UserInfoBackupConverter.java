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

package com.tle.core.usermanagement.standard.convert;

import com.tle.beans.Institution;
import com.tle.beans.user.UserInfoBackup;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.usermanagement.standard.dao.UserInfoBackupDao;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class UserInfoBackupConverter
    extends AbstractConverter<UserInfoBackupConverter.UserInfoBackupConverterInfo> {
  private static final String USER_INFO_BACKUP_FILE = "user_info_backup/user_info_backup.xml";
  private static final String USER_INFO_BACKUP_PLUGIN_ID = "USERINFOBACKUP";
  @Inject private UserInfoBackupDao userInfoBackupDao;

  @Override
  public void doExport(
      TemporaryFileHandle staging, Institution institution, ConverterParams callback)
      throws IOException {
    List<UserInfoBackup> allInfo = userInfoBackupDao.getAllInfo();
    xmlHelper.writeXmlFile(staging, USER_INFO_BACKUP_FILE, allInfo);
  }

  @Override
  public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
      throws IOException {
    if (!fileSystemService.fileExists(staging, USER_INFO_BACKUP_FILE)) {
      return;
    }

    final List<UserInfoBackup> allInfoBackup =
        xmlHelper.readXmlFile(staging, USER_INFO_BACKUP_FILE);
    for (UserInfoBackup userInfoBackup : allInfoBackup) {
      userInfoBackup.setInstitution_id(institution.getUniqueId());
    }
    final Collection<PostReadMigrator<UserInfoBackupConverterInfo>> migrations =
        getMigrations(params);
    runMigrations(migrations, new UserInfoBackupConverterInfo(allInfoBackup, params));

    for (UserInfoBackup userInfoBackup : allInfoBackup) {
      userInfoBackupDao.save(userInfoBackup);
      userInfoBackupDao.flush();
      userInfoBackupDao.clear();
    }
  }

  @Override
  public void doDelete(Institution institution, ConverterParams params) {
    userInfoBackupDao.deleteAllInfo();
  }

  @Override
  public String getStringId() {
    return USER_INFO_BACKUP_PLUGIN_ID;
  }

  public static class UserInfoBackupConverterInfo {
    private final List<UserInfoBackup> prefs;
    private final ConverterParams params;

    public UserInfoBackupConverterInfo(List<UserInfoBackup> prefs, ConverterParams params) {
      this.prefs = prefs;
      this.params = params;
    }

    public ConverterParams getParams() {
      return params;
    }

    public List<UserInfoBackup> getPrefs() {
      return prefs;
    }
  }
}
