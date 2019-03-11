package com.tle.web.api.interfaces.beans;

public class BaseEntityExportBean {
  @SuppressWarnings("nls")
  private String exportVersion = "1.0";

  private EntityLockBean lock;

  public String getExportVersion() {
    return exportVersion;
  }

  public void setExportVersion(String exportVersion) {
    this.exportVersion = exportVersion;
  }

  public EntityLockBean getLock() {
    return lock;
  }

  public void setLock(EntityLockBean lock) {
    this.lock = lock;
  }
}
